# Proximity Beacon Manager

This repository contains a sample beacon manager application on Android using Google's [Proximity Beacon API](https://developers.google.com/beacons/proximity/guides). The application exposes a few basic functions of the API:

* Registering new beacons
* Listing registered beacons
* Listing beacon attachments
* Listing project namespaces
* Creating & deleting beacon attachments

# Disclaimer

This repository contains sample code intended to demonstrate the capabilities of the Proximity Beacon API. It is not intended to be used as-is in applications as a library dependency—or a stand-alone production application—and will not be maintained as such. Bug fix contributions are welcome, but issues and feature requests will not be addressed.

# Sample Usage

The beacon API (and this sample application) uses your Google account and OAuth 2.0 to authenticate all requests. Because of this, you do not need to make any source code changes to use it. You must simply choose a Google account that is already attached to your developer's console project where the Proximity Beacon API is enabled.

For more information on enabling the API and creating the proper API credentials, follow the beacon API [Getting Started Guide](https://developers.google.com/beacons/proximity/get-started).

## Discovering Beacons

On your first run through the application (after selecting an account) there will likely be no beacons to list. Select _Register Beacon_ from the options menu to launch the beacon scanner. This activity will scan for advertising _Eddystone-UID_ beacons and allow you to register them with your console project.

## Managing Beacons

Once you have one or more beacons registered, you can tap on those beacons in the main list to access their attachments. You may create new attachments using the _add_ action in the options menu. Existing attachments can also be deleted from the attachments list.

To assist in making new attachments easier, tapping on an existing attachment will copy its contents to the clipboard so they can be pasted into the _create_ activity.

# License

The code supplied here is covered under the MIT Open Source License:

Copyright (c) 2015 Wireless Designs, LLC

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.