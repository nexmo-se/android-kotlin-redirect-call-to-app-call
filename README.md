# Vonage Android (kotlin) Call Redirection Demo

## Description

This Demo shows how to redirect phone calls to a Vonage app to phone call. After dialing a phone number, this demo app will open and will allow you to call the number through Vonage.

> **Note:** Uninstall the demo to have your calls function normally again.

## Pre-requisites
There are a few prerequisites that you need to complete before you can work through this tutorial. If you've already completed any of them, feel free to skip that step.
### Create a Vonage account

You can create a Vonage account via the  [Dashboard](https://ui.idp.vonage.com/ui/auth/registration?icid=tryitfree_adpdocs_nexmodashbdfreetrialsignup_inpagelink).

Within the Dashboard you can create Applications and purchase Vonage numbers. You can also perform these tasks using the  [Vonage CLI](https://github.com/vonage/vonage-cli).
### Install Node.js

If you want to carry out tasks such as creating applications, purchasing Vonage numbers and so on, you will need to install the Vonage CLI. As the Vonage CLI requires  `node.js`  you will need to  [install  `node.js`](https://nodejs.org/en/download/)  first.
### Install latest version of Vonage Command Line Interface (CLI)

The  [Vonage CLI](https://developer.vonage.com/en/application/vonage-cli)  allows you to carry out many operations on the command line. Examples include creating applications, purchasing numbers, and linking a number to an application.

To install the CLI with NPM run:

    npm install --location=global @vonage/cli

Set up the Vonage CLI to use your Vonage API Key and API Secret. You can get these from the  [settings page](https://dashboard.nexmo.com/settings)  in the Dashboard.

Run the following command in a terminal, while replacing  `API_KEY`  and  `API_SECRET`  with your own:

    vonage config:set --apiKey=API_KEY --apiSecret=API_SECRET

### Buy a Vonage number
#### - Using the Dashboard

First you can browse  [your existing numbers](https://dashboard.nexmo.com/your-numbers).

If you have no spare numbers you can  [buy one](https://dashboard.nexmo.com/buy-numbers).

#### - Using the Vonage CLI
You can purchase a number using the Vonage CLI. The following command purchases an available number in the US. Specify  [an alternate two-character country code](https://www.iban.com/country-codes)  to purchase a number in another country.

    vonage numbers:search US
    vonage numbers:buy 15555555555 US
    
### Install Android Studio

If you want to use the Client SDK for Android, you need to install  [Android Studio](https://developer.android.com/studio).

## Run the webhook server (call-redirect-webhook-server/app.js)
When an inbound call is received, Vonage makes a request to a publicly accessible URL of your choice - we call this the  `answer_url`. You need to create a webhook server that is capable of receiving this request and returning an  [NCCO](https://developer.vonage.com/en/voice/voice-api/ncco-reference)  containing a  `connect`  action that will forward the call to the  [PSTN phone number](https://developer.vonage.com/en/concepts/guides/glossary#virtual-number). You do this by extracting the destination number from the  `to`  query parameter and returning it in your response.

>**NOTE:**
For this project, the webhook server is  `/call-redirect-webhook-server/app.js`
For this demo, the webhook server can run via `localtunnel` for convenience. If you want to run this webhook server in a hosted webserver with its own domain or address, comment out lines after `//COMMENT THE FOLLOWING LINE IF HOSTING`


### Running the webserver
  1. Change to the webserver directory: `cd call-redirect-webhook-server`
  2. Init npm: `npm init`
  3. Install dependencies: `npm install express localtunnel --save`
  4. Inside apop.js, replace `SUBDOMAIN` with a random string of your choice between 4 and 20 alphanumeric characters (letters and numbers, not underscores or dashes) and `VONAGE_LVN_NUMBER` with your Vonage number in [E.164](https://en.wikipedia.org/wiki/E.164) format (e.g. 447700900000).
  5. Start the server: `node server.js`
A notice will be displayed telling you the server is now available: 
`App available at: https://SUBDOMAIN.loca.lt`
Please keep the terminal window handy as you will need the URL in the next step.

## Create your Vonage Application

In this step you will create a Vonage  [Application](https://developer.vonage.com/en/conversation/concepts/application)  capable of in-app voice communication use cases.

Open a new terminal and, if required, navigate to your project directory.

Create a Vonage application by copying and pasting the command below into terminal. Make sure to change the values of  `--voice_answer_url`  and  `--voice_event_url`  arguments, by replacing  `SUBDOMAIN`  with the actual value used in the previous step:


`vonage apps:create "App to Phone Tutorial" --voice_answer_url=https://SUBDOMAIN.loca.lt/voice/answer --voice_event_url=https://SUBDOMAIN.loca.lt/voice/even`t

> **NOTE:**  An application can be also created using the CLI's  [interactive mode](https://developer.vonage.com/en/application/vonage-cli#interactive-mode). For more details on how to create an application and various available application capabilities please see our  [documentation](https://developer.vonage.com/en/application/overview).

A file named  `vonage_app.json`  is created/updated in your project directory and contains the newly created Vonage Application ID and the private key. A private key file named  `app_to_phone_tutorial.key`  is also created.

Make a note of the Application ID that is echoed in your terminal when your application is created:

![screenshot of the terminal with Application ID underlined](https://developer.vonage.com/api/v1/developer/assets/screenshots/tutorials/client-sdk/vonage-application-created.png)

screenshot of the terminal with Application ID underlined

> **NOTE:**  Information about your application, including the Application ID, can also be found in the  [Dashboard](https://dashboard.nexmo.com/voice/your-applications).

## Link a Vonage number

### Using the Dashboard

1.  Find your application in the  [Dashboard](https://dashboard.nexmo.com/voice/your-applications).
2.  Click on the application in the Your Applications list. Then click on the Numbers tab.
3.  Click the Link button to link a Vonage number with that application.

### Using the Vonage CLI

Once you have a suitable number you can link it with your Vonage application. Replace  `YOUR_VONAGE_NUMBER`  with newly generated number, replace  `APPLICATION_ID`  with your application id and run this command:

```none
vonage apps:link APPLICATION_ID --number=YOUR_VONAGE_NUMBER
```

## Create a User

[Users](https://developer.vonage.com/en/conversation/concepts/user)  are a key concept when working with the Vonage Client SDKs. When a user authenticates with the Client SDK, the credentials provided identify them as a specific user. Each authenticated user will typically correspond to a single user in your users database.

To create a user named  `phonecaller`, run the following command using the Vonage CLI:



    vonage apps:users:create "phonecaller"

This will return a user ID similar to the following:

    User ID: USR-aaaaaaaa-bbbb-cccc-dddd-0123456789ab

## Generate a JWT

The Client SDK uses  [JWTs](https://developer.vonage.com/en/concepts/guides/authentication#json-web-tokens-jwt)  for authentication. The JWT identifies the user name, the associated application ID and the permissions granted to the user. It is signed using your private key to prove that it is a valid token.

Run the following commands, remember to replace the  `APPLICATION_ID`  variable with id of your application and  `PRIVATE_KEY`  with the name of your private key file.

> **NOTE**: We'll be creating a one-time use JWT on this page for testing. In production apps, your server should expose an endpoint that generates a JWT for each client request.

You are generating a JWT using the Vonage CLI by running the following command but remember to replace the  `APP_ID`  variable with your own value:


    vonage jwt --app_id=APPLICATION_ID --subject=phonecaller--key_file=./PRIVATE_KEY --acl='{"paths":{"/*/users/**":{},"/*/conversations/**":{},"/*/sessions/**":{},"/*/devices/**":{},"/*/image/**":{},"/*/media/**":{},"/*/applications/**":{},"/*/push/**":{},"/*

The above commands set the expiry of the JWT to one day from now, which is the maximum.

![terminal screenshot of a generated sample JWT](https://developer.vonage.com/api/v1/developer/assets/screenshots/tutorials/client-sdk/generated-jwt-key-vonage.png)

terminal screenshot of a generated sample JWT

## Running the Android Code
After Cloning this repository, open Android Studio. You can then open this Project via `File > Open` and selecting the directory where this repository was cloned.

Open `\app\src\main\java\com\vonage\tutorial\voice\dialertophone\CallActivity.kt` and replace `JWT` with the JWT you generated.

Sync Gradle, Attach your phone, Build and Run 

**Note:**  When asked for application to handle the call, select this application. Agree to all the permissions and you  can now redirect your call to this app.
