const MulticastClient = require('./multicast-client');
const config = require('./config');
const { Analyser, Message, Interval } = require('./analyser');
const fs = require('fs');
const path = require('path');


const startUpTime = new Date().getTime();

fs.mkdirSync(path.join(__dirname, '../files'));

class ControlMessage extends Message {
  constructor (message)
  {
    super(message);
    this.status = message.status;
  }
}

const controlAnalyser = new Analyser();

new MulticastClient(config.address, config.control)
  .receive(message => controlAnalyser.addMessage(new ControlMessage(message)));

function parseTime(time) {
  return new Date(time).toISOString().replace(/T/, ' ').replace(/\..+/, '');
}

setInterval(() => {
  if (controlAnalyser.messageCount <= 1) return;

  console.log('\nAverage delay: %s seconds', controlAnalyser.averageDelay / 1000);

  const lostMessages = controlAnalyser.lostMessages ? controlAnalyser.lostMessages.length : undefined;
  console.log('Local counter: %s, IAS counter: %s, Lost: %s, Unordered: %s', controlAnalyser.messageCount,
    controlAnalyser.lastMessage.counter, lostMessages, controlAnalyser.unorderedMessages.length);
  
  if (controlAnalyser.longDelayIntervals.length > 0)
  {
    console.log('Long delay intervals:');

    controlAnalyser.longDelayIntervals.forEach(interval => {
      const begin = parseTime(interval.begin);
      const end = parseTime(interval.end);
      
      console.log('\t%s - %s : %s seconds', begin, end, interval.time / 1000);
      return ;
    });
  }

  fs.writeFile(path.join(__dirname, `../files/control_${startUpTime}.json`), JSON.stringify(controlAnalyser), error => {
    if (error) console.log('Could not save progress', error);
  });
}, 3000);
