const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const path = require('node:path');

class Element {
  constructor() { this.textContent = ''; this.hidden = false; this.children = []; this.events = {}; this.classList = {toggle(){}}; }
  addEventListener(name, fn) { this.events[name] = fn; }
  append(...items) { items.forEach(item => { item.parent = this; this.children.push(item); }); }
  prepend(item) { item.parent = this; this.children.unshift(item); }
  replaceChildren() { this.children = []; }
  remove() { if(this.parent) this.parent.children = this.parent.children.filter(item=>item!==this); }
}
function environment(config, authority = false) {
  const elements = new Map();
  const get = id => { if(!elements.has(id)) elements.set(id,new Element()); return elements.get(id); };
  let receive;
  const context = {
    window: {},
    document: {getElementById:get,querySelector:()=>null,createElement:()=>new Element(),body:{hasAttribute:()=>false}},
    navigator: {geolocation:{getCurrentPosition:resolve=>resolve({coords:{latitude:3.8,longitude:11.5}})}},
    fetch: async()=>({ok:true,redirected:false,headers:{get:()=> 'application/json'}}),
    console
  };
  context.window[authority ? 'shepherdAuthority' : 'shepherdCitizen'] = config;
  context.window.ShepherdSafety = {
    createMap:()=>null, enableAudio:()=>false,playSound(){},setConnection(){},
    validLocation:a=>Number.isFinite(a.lat)&&Number.isFinite(a.lng),
    alertDetails:()=>new Element(),connect:(topic,fn)=>{receive=fn;}
  };
  vm.createContext(context);
  vm.runInContext(fs.readFileSync(path.resolve(__dirname,'../../main/resources/static/js',authority?'authority-dashboard.js':'citizen-dashboard.js'),'utf8'),context);
  return {context,get,receive:alert=>receive(alert)};
}

test('active alert restores Cancel button and cancels without asking for location', async()=>{
  const env=environment({userId:'citizen',active:true});
  env.context.navigator.geolocation.getCurrentPosition=()=>{throw Error('Location should not be requested');};
  let endpoint;env.context.fetch=async url=>{endpoint=url;return {ok:true};};
  assert.equal(env.get('distressBtn').textContent,'Cancel alert');
  await env.get('distressBtn').events.click();
  assert.equal(endpoint,'/api/alert/cancel');
  assert.equal(env.get('status').textContent,'Alert cancelled.');
  assert.equal(env.get('distressBtn').disabled,false);
});
test('location denial does not send a request and permits retry', async()=>{
  const env=environment({userId:'citizen',active:false});let called=false;
  env.context.navigator.geolocation.getCurrentPosition=(success,error)=>error({code:1});
  env.context.fetch=async()=>{called=true;};
  await env.get('distressBtn').events.click();
  assert.equal(called,false);assert.match(env.get('status').textContent,/Allow location access/);
  assert.equal(env.get('distressBtn').disabled,false);
});
test('failed request keeps the alert state and allows retry', async()=>{
  const env=environment({userId:'citizen',active:false});
  env.context.fetch=async()=>({ok:false});await env.get('distressBtn').events.click();
  assert.equal(env.get('distressBtn').textContent,'Send emergency alert');
  assert.match(env.get('status').textContent,/not confirmed/);assert.equal(env.get('distressBtn').disabled,false);
});
test('login redirects are reported as expired sessions instead of successful alerts', async()=>{
  const env=environment({userId:'citizen',active:false});
  env.context.fetch=async()=>({ok:true,redirected:true});await env.get('distressBtn').events.click();
  assert.match(env.get('status').textContent,/session has expired/);
  assert.equal(env.get('distressBtn').textContent,'Send emergency alert');
});
test('authority shows existing alerts once and removes cancelled user alerts',()=>{
  const alert={alertId:'alert1',userId:'citizen',quarterId:'quarter',lat:3.8,lng:11.5};
  const env=environment({quarterId:'quarter',alerts:[alert]},true);
  assert.equal(env.get('alertCount').textContent,1);assert.equal(env.get('alertsEmpty').hidden,true);
  env.receive(alert);assert.equal(env.get('alerts').children.length,1);
  env.receive({quarterId:'quarter',userId:'citizen',cancel:true});
  assert.equal(env.get('alertCount').textContent,0);assert.equal(env.get('alertsEmpty').hidden,false);
});
test('authority ignores alerts from a different jurisdiction',()=>{
  const env=environment({quarterId:'quarter',alerts:[]},true);
  env.receive({alertId:'other',userId:'other',quarterId:'elsewhere',lat:3.8,lng:11.5});
  assert.equal(env.get('alerts').children.length,0);
});
test('cancelling one family alert does not clear another household alert',()=>{
  const env=environment({userId:'self',familyId:'family',active:false});
  env.receive({alertId:'one',userId:'one',lat:3.8,lng:11.5});
  env.receive({alertId:'two',userId:'two',lat:3.8,lng:11.5});
  env.receive({cancel:true,userId:'one'});
  assert.equal(env.get('familyAlerts').children.length,1);
  assert.equal(env.get('familyAlertsEmpty').hidden,true);
});
