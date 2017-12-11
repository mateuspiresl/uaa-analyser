const dgram = require('dgram');


const LOCAL = '127.0.0.1';

class MulticastClient
{
  constructor (address, port)
  {
    this.address = address;
    this.port = port;
    this.client = dgram.createSocket('udp4');
  }

  receive (callback) {
    return new Promise((resolve, reject) => {
      this.client.on('listening', () => {
        this.client.setBroadcast(true)
        this.client.setMulticastTTL(128); 
        this.client.addMembership(this.address);
        
        resolve(this.client.address());
      });
      this.client.on('message', (message, info) => callback(JSON.parse(message), info));
      this.client.bind(this.port);
    });
  }

  close () {
    this.client.close();
  }
}

module.exports = MulticastClient;
