const express = require('express');
const app = express();
const port = 3001;
const vonage_number = "VONAGE_LVN_NUMBER"
const subdomain = "SUBDOMAIN";

app.use(express.json());
app.use(express.urlencoded({ extended: false }));


function getHost(req) {
  var url = req.protocol + '://' + req.get('host')
  return url
}

call_ncco = (hostUrl, number) => {
  return [
    {
      "action": "talk",
      "text": "Please wait while we connect you."
    },
    {
      "action": "connect",
      "eventUrl": [hostUrl + "/voice/event"],
      "timeout": "45",
      "from": vonage_number,
      "endpoint": [
        {
          "type": "phone",
          "number": number,
        }
      ]
    }
  ]
}


app.get('/voice/answer', (req, res) => {
  console.log('NCCO request:');
  console.log(`  - callee: ${req.query.to}`);
  console.log('---');
  res.json(call_ncco(getHost(req), req.query.to));
});

app.all('/voice/event', (req, res) => {
  console.log('EVENT:');
  console.dir(req.body);
  console.log('---');
  res.sendStatus(200);
});



app.listen(port, () => {
  console.log(`Answering Machine Demo app listening on port ${port}`)
  console.log(``)
})

//COMMENT THE FOLLOWING LINES IF HOSTING
const localtunnel = require('localtunnel');
(async () => {
  const tunnel = await localtunnel({
      subdomain: subdomain,
      port: port
    });
  console.log(`App available at: ${tunnel.url}`);
})();