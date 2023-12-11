import _1 from "discord.js";
const { Client } = _1;
import fs from "node:fs";
import { Server } from "node:net";


const client = new Client({ intents: 0x8201 });

const utf8enc = TextEncoder.prototype.encode.bind(new TextEncoder())
const utf8dec = TextDecoder.prototype.decode.bind(new TextDecoder())

// discord token
const token = "uhhh";

// -Dlwbridge.host
const host = "127.0.0.1";
// -Dlwbridge.port
const port = 48217;

// channel id of target
const chid = "1183341391980540054";
let ch;

client.login(token);

function recv(txt) {
	// filter out minecraft escape codes (for now)
	ch.send(txt.replace(/§./g, ""));
}
client.on("messageCreate", async m=>{
	if(m.channelId != chid) return;
	if(m.author.id == client.user.id) return;
	const ls = m.attachments.map(e=>e.url).join(", ");
	let _, ru, rc;
	if(m.reference?.messageId) {
		try {
			const rm = await m.fetchReference();
			if(rm.author.id == client.user.id) {
				[_, ru, rc] = rm.content.match(/^(?:<(.+?)> )?(.+)$/s);
				ru = ru.replace(/\\([\\_*~])/g, "$1")
			} else {
				ru = rm.author.username + (+m.author.discriminator == 0 ? "" : "#" + m.author.discriminator);
				rc = rm.content
			}
		} catch(e) {
			console.log(e);
			rc = "<The message failed to load.>";
		}
	}
	let s = m.content;
	if(rc) s = `(to: <${ru}> ${rc}) ${s}`;
	if(ls) s += ` (${ls})`;
	let u = m.author.username + (m.author.discriminator == 0 ? "" : "#" + m.author.discriminator);
	send(u, s);
});


let cc;
const s = new Server();
s.on("connection", c=>{
	if(cc) return c.destroy();
	cc = c;
	console.log(`server connected ${c.remoteAddress}:${c.remotePort}`);
	c.on("end", ()=>{
		console.log(`server disconnected`);
	});
	c.on("error", e=>{
		console.log(`server disconnected, err ${e}`);
	});
	c.on("data", l=>{
		let a = utf8dec(l).split('\n');
		for(let x of a) {
			if(x.trim()) recv(x);
		}
	});

});
function send(u, s) {
	for(let a of s.trim().split("\n")) if(a.trim()) { cc?.write(`${u}\0${a}\n`); u = "" }
}
s.listen({host, port});
console.log(`listening on ${host}:${port}`);

await new Promise(a=>client.once("ready", a));
console.log(`discord ready ${client.user.tag}`);
ch = await client.channels.fetch(chid);
