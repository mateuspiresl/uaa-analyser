const MulticastClient = require('./multicast-client');
const config = require('./config');
const fs = require('fs');
const path = require('path');


const MAX_DELAY = 2750;

class Message {
  constructor (message) {
    this.time = new Date().getTime();
    this.counter = parseInt(message.counter);
  }
}

class Interval {
  constructor (begin, end) {
    this.begin = begin;
    this.end = end;
  }

  get time () {
    return this.end - this.begin;
  }
}

class Analyser {
  constructor (saved)
  {
    this.lastMessage = saved ? saved.lastMessage : null;
    this.messageCount = saved ? saved.messageCount : 0;;
    this.longDelayIntervals = saved ? saved.longDelayIntervals : [];
    this.lostMessages = saved ? saved.lostMessages : [];
    this.unorderedMessages = saved ? saved.unorderedMessages : [];
    
    this._delaySum = saved ? saved._delaySum : 0;
    this._delayCount = saved ? saved._delayCount : 0;
  }

  get averageDelay ()
  {
    if (this._delayCount === 0) return 0;
    return this._delaySum / this._delayCount;
  }

  addMessage (message)
  {
    if (this.messageCount > 0)
    {
      if (message.counter < this.lastMessage.counter)
      {
        console.log('Received unordered message (%s)', message.counter);
        this.unorderedMessages.push(message.count);

        const unorderedIndex = this.lostMessages.indexOf(message.counter);
        if (unorderedIndex >= 0) this.lostMessages.splice(unorderedIndex, 1);
      }
      else
      {
        const delay = message.time - this.lastMessage.time;
  
        if (delay > MAX_DELAY)
          this.longDelayIntervals.push(new Interval(this.lastMessage.time, message.time))
        else {
          this._delaySum += delay;
          this._delayCount += 1;
        }
  
        let lost = this.lastMessage.counter + 1;
        while (lost < message.counter) this.lostMessages.push(lost++);
      }
    }

    this.lastMessage = message;
    this.messageCount++;
  }
}


exports.Analyser = Analyser;
exports.Message = Message;
exports.Interval = Interval;