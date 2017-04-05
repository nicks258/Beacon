CHANGELOG
=========

v. 0.6.0
--------
*03 Apr 2017*
- migrated library to RxJava2.x on RxJava2.x branch and released it as `reactivebeacons-rx2` artifact
- kept library compatible with RxJava1.x on a RxJava1.x branch and released it as `reactivebeacons` artifact
- removed master branch
- bumped library dependencies
- added permission annotations
- organized gradle configuration
- transformed instrumentation unit tests to pure java unit tests
- started executing unit tests on Travis CI server
- created separate JavaDoc for RxJava1.x and RxJava2.x

v. 0.5.1
--------
*31 Jul 2016*

- bumped RxJava to v. 1.1.8
- bumped Truth to v. 0.28
- bumped compile SDK version to 23
- updated dependencies in sample apps
- updated documentation

v. 0.5.0
--------
*27 Mar 2016*

- added support for BLE scanning API available on Android API 21 (Lollipop) mentioned in issue #43
- created two scanning strategies for BLE scanning on pre-Lollipop devices and Lollipop devices
- updated body of `Observable<Beacon> observe()` method
- added `PreLollipopScanStrategy` class, `LollipopScanStrategy` class and `ScanStrategy` interface
- added `ScanCallbackAdapter` class used in `LollipopScanStrategy`
- added `Observable<Beacon> observe(ScanStrategy scanStrategy)` method to library API, which allows to use one of the existing scan strategies or custom scan strategy
- added note about Compatibility with different Android versions in `REAMDE.md`
- bumped RxJava version to `1.1.2`
- bumped Kotlin version in sample app to `1.0.1-1`
- changed `Schedulers.io()` in `subscribeOn(...)` to `Schedulers.computation()` in sample apps

v. 0.4.1
--------
*13 Dec 2015*

- bumped RxJava dependency to v. 1.1.0
- bumped Google Truth test dependency to v. 0.27
- bumped RxAndroid dependency in code samples to v. 1.1.0
- bumped Gradle Build Tools version to v. 1.3.1

v. 0.4.0
--------
*15 Nov 2015*

- added `MacAddress` class with MAC address validation
- added `macAddress` field to `Beacon` class
- added `exceptName(final String... names)` method to `Filter` class
- added `exceptMacAddress(final String... macs)` method to `Filter` class
- added `hasMacAddress(final MacAddress... macs)` method to `Filter` class
- added `exceptMacAddress(final MacAddress... macs)` method to `Filter` class 

v. 0.3.2
--------
*11 Nov 2015*

- bug fix: wrapped `BluetoothManager` inside `isBleSupported()` to avoid `NoClassDefFound` error occurring while instantiating `ReactiveBeacons` object on devices running API < 18 - fixed in PR #30.

v. 0.3.1
--------
*09 Nov 2015*

- improved filters in PR #24
- fixed RxJava usage in sample app
- fixed RxJava usage in code snippets in `README.md`
- added static code analysis
- added sample app in Kotlin
- added sample app with Android Service

v. 0.3.0
--------
*25 Oct 2015*

- replaced `distinct()` operator with `distinctUntilChanged()` operator in `Observable<Beacon> observe()` method in `ReactiveBeacons` class.
- added permissions `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` to satisfy requirements of Android 6
- renamed `void requestBluetoothAccessIfDisabled(activity)` method to `void requestBluetoothAccess(activity)`
- added `boolean isBluetoothEnabled()` method
- added `boolean isLocationEnabled(context)` method
- added `void requestLocationAccess(activity)` method
- modified sample app in order to make it work on Android 6 Marshmallow
- reduced target API from 23 to 22 in library due to problems with additional permissions and new permission model (it can be subject of improvements in the next releases)
- added package private `AccessRequester` class

v. 0.2.0
--------
*11 Oct 2015*

- decreased min SDK version to 9
- added `isBleSupported()` method to the public API
- if BLE is not supported by the device, library emits an empty Observable
- updated exemplary app
- updated documentation in `README.md` file

v. 0.1.0
--------
*05 Oct 2015*

- added `Filter` class providing methods, which can be used with `filter(...)` method from RxJava inside specific subscription. These methods can be used for filtering stream of Beacons by Proximity, distance, device names and MAC addresses.
- added missing `reactivebeacons` package to library module


v. 0.0.1
--------
*29 Sep 2015*

First release of the library.
