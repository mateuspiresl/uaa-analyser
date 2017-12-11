const fs = require('fs');
const path = require('path');
const { Analyser, Message, Interval } = require('./analyser');


const fileName = process.argv[2];
if (!fileName)
{
  console.log('Must provide a file to read');
  process.exit(1);
}

console.log('Opening file %s', fileName);
const fileContent = fs.readFileSync(path.join(__dirname, `../files/${fileName}`));

const controlAnalyser = new Analyser(JSON.parse(fileContent));
controlAnalyser.longDelayIntervals = controlAnalyser.longDelayIntervals.map(interval => {
  return new Interval(interval.begin, interval.end);
});

function parseTime(time) {
  return new Date(time).toISOString().replace(/T/, ' ').replace(/\..+/, '');
}

if (controlAnalyser.messageCount   > 1)
{
  console.log('\nAverage delay: %s seconds', controlAnalyser.averageDelay / 1000);
  
  const lostMessages = controlAnalyser.lostMessages ? controlAnalyser.lostMessages.length : undefined;
  console.log('Local counter: %s\nIAS counter: %s\nLost: %s', controlAnalyser.messageCount,
    controlAnalyser.lastMessage.counter, lostMessages);
  
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
}
else {
  console.log('Not enough messages')
}