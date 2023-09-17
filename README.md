# ns_upi

Find installed UPI payment apps on your phone and make payments using any one of them.

## Getting Started

Add this package to your flutter project's `pubspec.yaml` as a dependency as follows:

```yaml
dependencies:
  ...
  ns_upi: ^1.0.0
```

Import the package as follows:

```dart
import 'package:ns_upi/ns_upi.dart';
```

### iOS configuration

In `Runner/Info.plist` add or modify the `LSApplicationQueriesSchemes` key so it includes custom query schemes shown as follows:

```xml
...
    <key>LSApplicationQueriesSchemes</key>
    <array>
        <string>Bandhan</string>
        <string>BHIM</string>
        <string>CanaraMobility</string>
        <string>CentUPI</string>
        <string>com.amazon.mobile.shopping</string>
        <string>com.ausmallfinancebank.aupay.bhimupi</string>
        <string>com.jkbank.bhimjkbankupi</string>
        <string>com.rbl.rblimplicitjourney</string>
        <string>com.syndicate.syndupi</string>
        <string>com.vijayabank.UPI</string>
        <string>cred</string>
        <string>dbin</string>
        <string>freecharge</string>
        <string>gpay</string>
        <string>hdfcnewbb</string>
        <string>imobileapp</string>
        <string>in.cointab.app</string>
        <string>in.fampay.app</string>
        <string>kvb.app.upiapp</string>
        <string>lotza</string>
        <string>mobikwik</string>
        <string>money.bullet</string>
        <string>myairtel</string>
        <string>myJio</string>
        <string>paytm</string>
        <string>payzapp</string>
        <string>phonepe</string>
        <string>truecaller</string>
        <string>ucoupi</string>
        <string>upi</string>
        <string>upibillpay</string>
        <string>whatsapp</string>
        <string>www.citruspay.com</string>
    </array>
...
```

### Usage

#### Get list of installed apps

```dart
final List<ApplicationMeta> appMetaList = await NsUpi.getInstalledUpiApps();
```

#### Do a UPI transaction

```dart
Future<void> upiTransaction(ApplicationMeta appMeta) {
  final UpiTransactionResponse response = await NsUpi.initiateTransaction(
    amount: '1.00',
    app: appMeta.application,
    receiverName: 'Ajay Kumar',
    receiverUpiAddress: '7875056731@paytm',
    transactionRef: 'NONSTOPIO000001',
    transactionNote: 'A TEST UPI Transaction',
  );
  debugPrint(response.status);
}
```