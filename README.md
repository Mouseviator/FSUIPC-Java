## Introduction

What? What is [FSUIPC](http://www.fsuipc.com/)? Well, I think, if you ask any at least middle-core flight simmer, will tell you it is the must-have add-on for flight simulator (FSX, Prepar3D and others) developed by Pete and John Dowson. Java is programming language and SDK stands for Software Development Kit. So putting all of this together, with FSUIPC Java SDK you can write your own programs in Java that will utilize FSUIPC ??

But wait… I know about these stuff and there already is an Java SDK for FSUIPC, so what is this about? You are right, there is. But, well, I am lazy and I do not want to write the same explanation over and over (I did write it in the readme, and source files…), so let me let you read the explanation from the README file included with this SDK (actually, most of the text on this page is simply copied from the Readme file, while to write it twice diffrently, right).

This whole Java FSUIPC SDK is based upon SDK written by Mark Burton, later amended by Paul Henty for 64 bit environment. But, it has been greatly rewritten and uses different approach.

*Why the rewrite? Well, the main reason for me was performance considerations. After studying FSUIPC C SDK, the wrapper library and the Java SDK by Mark, I found out that the C/C++ wrapper library (**fsuipc_java64.dll**, **fsuipc_java32.dll**), that implements the **FSUIPCWrapper.readData** and **FSUIPCWrapper.writeData** functions, calls the **FSUIPC_Process** function. What is the catch? Well, normally when working with the FSUIPC C/C++ SDK, you call **FSUIPC_Read** and **FSUIPC_Write** functions to tell FSUIPC what data you want to read/write, kind of registering data requests, and than process all of them using the **FSUIPC_Process** function. I have read on the forums that this function is quite heavy on processing time – thus, it is not good to call it very often. But this is how it was implemented in the previous wrapper libraries. The **FSUIPC_Process** function **got called every time you called the read or write functions**. It may not cause trouble in simple projects. But, when you would read multiple values, continuously, like in the loop, for example for flight monitoring app, I think that might be a performance issue sooner or later. Not mentioning, that your app probably would not be the only one that FSUIPC would have to provides it’s services to. That is why I decided to try to write a different SDK, that will try to reflect the approach that we would use when writing the code in C/C++. This SDK thinks of FSUIPC read/writes as of data requests. It allows you to register multiple of them and then process them all via one call of the process function.*

**NOTE:** *This SDK is NOT considered to be complete! While I believe it contains solid foundation for developing applications using FSUIPC in Java, it is far away from complete. There are just a couple of helper classes which are more of an examples of what to do with the “data requests”, how to write them. It would be nearly impossible for me to cover all, most of FSUIPC offsets with helper classes and also test them. Look on the FSUIPC.jar library source files, on the source of helper classes to see how they are implemented.*

## License

Well, the API from Mark Burton and Paul Henty were given for free, this one is also FREE. If you want to be specific, than say it is LGPL.

## The folders

* **FSUIPC_Java_dist** – the directory with pre-compiled Java package. It should contain the **FSUIPC.jar, FSUIPC-javadoc.jar (the javadoc packed in .jar), FSUIPC-sources.jar (source packed in .jar), FSUIPC-test.jar (JUnit tests), fsuipc_java32.dll, fsuipc_java64.dll** and **javadoc** folder. These files are ready to use in your project. The FSUIPC library was last compiled unde JDK 15 ([AdoptOpenJDK](https://adoptopenjdk.net/) 15.0.1.9 Hotspot). Note that for production, only the FSUIPC.jar, fsuipc_java32.dll, fsuipc_java64.dll are need.
* **FSUIPC** – the directory containing the source code of the FSUIPC.jar library. The project is for [Netbeans](https://netbeans.org/) (Apache Netbeans 12 – to be specific). It was written using JDK 11 , compiled with AdoptOpenJDK 15.0.1.9 Hotspot, and tested also with 32bit JDK 15 ([AdoptOpenJDK](https://adoptopenjdk.net/) 15.0.1.9 Hotspot). This folder contains 3 batch files – **Make JavaDoc.cmd** – to make Javadoc (but not needed actually, Netbeans can do that if setup correctly). The **MakeHeaderFiles32.cmd** and **MakeHeaderFiles64.cmd** will create the header file for the FSUIPCWrapper class (that is the one containing native functions) using the 32/64bit JDK. These header files are then used in the **CWrapper32** (fsuipc_java32.dll) and **CWrapper64** (fsuipc_java64.dll) C++ projects, which implements the native functions. Note that all of these batch files contains absolute paths on my system and therefore WILL NEED adjustments for your system.
* **CWrapper32** – Contains the source code for the **fsuipc_java32.dll** – the 32bit library version that implements native functions of the **FSUIPCWrapper** java class. It is written in C++. The project is for [Visual Studio](https://visualstudio.microsoft.com/) (C++) 2019. When you open this project in Visual Studio, it will probably need some settings adjustment, as some paths will be different on your system than on mine, but skill-full developer like you will have not big issues with that, I am sure. For sure you will have to set paths to Java JDK header files, so that C++ knows about them (In VS 2019 this is under project properties -> C/C++ -> General -> Additional Include Directories). The folder is pretty BIG as it contains the packages for boost libraries, that the library uses for logging purposes.
* **CWrapper64** – Contains the source code for the **fsuipc_java64.dll** – the 64bit library version that implements native functions of the **FSUIPCWrapper** java class. It is written in C++. The project is for [Visual Studio](https://visualstudio.microsoft.com/) (C++) 2019. When you open this project in Visual Studio, it will probably need some settings adjustment, as some paths will be different on your system than on mine, but skill-full developer like you will have not big issues with that, I am sure. For sure you will have to set paths to Java JDK header files, so that C++ knows about them (In VS 2019 this is under project properties -> C/C++ -> General -> Additional Include Directories). The folder is pretty BIG as it contains the packages for boost libraries, that the library uses for logging purposes.
* **FSUIPCSimpleTest** – contains simple sample application that shows the usage of some FSUIPC functionality. It shows the basics of “FSUIPC data request” concept of this SDK, the connection to FSUIPC and reading one time data requests. The project is for Netbeans (Apache Netbeans 12 – to be specific). Some settings adjustment will be required after opening the project. I had the FSUIPC project set as dependency, you can do the same or you can point it to pre-compiled FSUIPC.jar from **FSUIPC_Java_dist**.
* **FSUIPCSimMonitor** – contains more complex example of FSUIPC library usage. Most SDK are shipped with basic examples, which really does not show you much. Well, I tried to do better here ?? This is SWING GUI application, with map. It will wait for successful FSUIPC connection and then show various aircraft and sim data, updating aircraft position on map. Shows even more from the concept of FSUIPC data requests of this SDK – the continual data requests and FSUIPC listener. The project is for Netbeans (Apache Netbeans 12 – to be specific). Some settings adjustment will be required after opening the project. I had the FSUIPC project set as dependency, you can do the same or you can point it to pre-compiled FSUIPC.jar from **FSUIPC_Java_dist**. This example app actually shows also a write requests. You can Pause the sim using the Pause button or toggle the Slew mode using the Slew button. 
* **C++ Memory Validator Reports** – this folder contains exported reports from the [C++ Memory Validator software](https://www.softwareverify.com/cpp-memory.php), that I used to monitor the **fsuipc_java32.dll** and **fsuipc_java64.dll** for memory leaks while running the FSUIPC Sim Monitor example for about an hour. There are HTML reports, ans also stored sessions, which you might be able to load in the software, if you own it.

## API documentation

You will find that inside the **javadoc** folder in the **FSUIPC_Java_dist** folder. Read especially the documentation of the **FSUIPC** class in the **com.mouseviator.fsuipc** package, as it is the core class and the doc has brief samples in it. Or you can read it online [here](https://www.mouseviator.com/wp-content/uploads/documents/fsuipc_java_sdk_javadoc/index.html).

## How to use the SDK?

Here I would like to show you some basic steps on how to use this SDK. Study the **FSUIPCSimpleTest** and **FSUIPCSimMonitor** example applications to get full (or at least better) picture. The text below is simply copied form the javadoc of [FSUIPC](https://www.mouseviator.com/wp-content/uploads/documents/fsuipc_java_sdk_javadoc/com/mouseviator/fsuipc/FSUIPC.html) class.

First of all, we need to get an instance of **FSUIPC** class:

`FSUIPC fsuipc = FSUIPC.getInstance();`

Than we need to load the native library. In the code below, we do that using the [load()](https://www.mouseviator.com/wp-content/uploads/documents/fsuipc_java_sdk_javadoc/com/mouseviator/fsuipc/FSUIPC.html#load()) function, which will try to determine whether to load 32 bit, or 64 bit library automatically. But you can also use [load32()](https://www.mouseviator.com/wp-content/uploads/documents/fsuipc_java_sdk_javadoc/com/mouseviator/fsuipc/FSUIPC.html#load32()) or [load64()](https://www.mouseviator.com/wp-content/uploads/documents/fsuipc_java_sdk_javadoc/com/mouseviator/fsuipc/FSUIPC.html#load64()) to load specific version if you have your own logic for determining JVM platform. When the library is loaded, we can connect to FSUIPC.

```
byte result = FSUIPC.load();
if (result != FSUIPC.LIB_LOAD_RESULT_OK) {
    System.out.println("Failed to load native library. Quiting...");
    return;
}

int ret = fsuipc.connect(FSUIPCWrapper.FSUIPCSimVersion.SIM_ANY);
if (ret == 0) {
    System.out.println("Flight sim not found");
} else {
    System.out.println("Flight Sim found!");
}
```

Now, we can create some data request to read/write some data to/from simulator. In the code below, we create data request to read airspeed and register it for one time processing.

```
//Helper for gathering aircraft data
AircraftHelper aircraftHelper = new AircraftHelper();

//Get IAS data request and register it for time processing
FloatRequest ias = (FloatRequest) fsuipc.addOneTimeRequest(aircraftHelper.getIAS());

//Let FSUIPC process all one-time requests
int ret = fsuipc.processRequestsOnce();

//Later on, print the requested data
if (ret == FSUIPC.PROCESS_RESULT_OK) {
    System.out.println("Aircraft IAS: " + String.valueOf(ias.getValue()));
}
```

Note that in the code above, we are using the helper method [AircraftHelper.getIAS()](https://www.mouseviator.com/wp-content/uploads/documents/fsuipc_java_sdk_javadoc/com/mouseviator/fsuipc/helpers/aircraft/AircraftHelper.html#getIAS()) to get the IAS data request. That helper provides modified request object, that will return the IAS in Kts as float value, even though that FSUIPC itself will return it as integer value * 128. The modified data request [IDataRequest.getValue()](https://www.mouseviator.com/wp-content/uploads/documents/fsuipc_java_sdk_javadoc/com/mouseviator/fsuipc/datarequest/IDataRequest.html#getValue()) will perform the calculation for us. The code above is the same as this:

```
//Get IAS data request and register it for time processing
IntRequest ias = new IntRequest(0x02BC);
fsuipc.addOneTimeRequest(ias);

//Let FSUIPC process all one-time requests
int ret = fsuipc.processRequestsOnce();

//Later on, print the requested data
if (ret == FSUIPC.PROCESS_RESULT_OK) {
   System.out.println("Aircraft IAS (method 2): " + String.valueOf(ias.getValue() / 128.0f));
}
```

If we want to monitor the airspeed continuously, we would register it with [addContinualRequest(com.mouseviator.fsuipc.datarequest.IDataRequest)](https://www.mouseviator.com/wp-content/uploads/documents/fsuipc_java_sdk_javadoc/com/mouseviator/fsuipc/FSUIPC.html#addContinualRequest(com.mouseviator.fsuipc.datarequest.IDataRequest)) rather than with [addOneTimeRequest(com.mouseviator.fsuipc.datarequest.IDataRequest)](https://www.mouseviator.com/wp-content/uploads/documents/fsuipc_java_sdk_javadoc/com/mouseviator/fsuipc/FSUIPC.html#addOneTimeRequest(com.mouseviator.fsuipc.datarequest.IDataRequest)). In this case, it would also be nice to define the listener so that we know when processing happened. Note that the example uses logger variable, which is for logging messages (if you don’t know about logging in Java, just ignore it) – kind of instead of System.out.println… Also notice, that inside the [IFSUIPCListener.onProcess(java.util.AbstractQueue)](https://www.mouseviator.com/wp-content/uploads/documents/fsuipc_java_sdk_javadoc/com/mouseviator/fsuipc/IFSUIPCListener.html#onProcess(java.util.AbstractQueue)) callback we use SwingUtilities.invokeLater. This is because the processing happens in FSUIPC class processing thread – NOT EDT thread… and all GUI updates should happen on EDT thread…that is how to do it. The last line of code in this example, that cancels the requests processing, is not really needed. The FSUIPC class check the last result code after each call to FSUIPC’s processing function and it will cancel the processing thread once it finds out that FSUIPC connection to sim has been lost. But you can of course call it if you want to stop the processing for any reason.

```
//Helper for gathering aircraft data
AircraftHelper aircraftHelper = new AircraftHelper();

//Get IAS data request and register it for continual processing
FloatRequest ias = (FloatRequest) fsuipc.addContinualRequest(aircraftHelper.getIAS());

//We will get results from continual request processing using the listener
IFSUIPCListener fsuipcListener = new IFSUIPCListener() {
       @Override
       public void onConnected() {
          logger.info("FSUIPC connected!");
       }

       @Override
       public void onDisconnected() {
          logger.info("FSUIPC disconnected!");
       }

       @Override
       public void onProcess(AbstractQueue<IDataRequest> arRequests) {
          System.out.println("FSUIPC continual request processing callback!");

          //GUI updates on EDT thread
          SwingUtilities.invokeLater(new Runnable() {
              @Override
              public void run() {
                  lblIAS.setText(String.format("%d Kts", (int) Math.ceil(ias.getValue())));
              }
          }
       }

       @Override
       public void onFail(int lastResult) {
           logger.log(Level.INFO, "Last FSUIPC function call ended with error code: {0}, message: {1}",
                 new Object[]{lastResult,
                    FSUIPC.FSUIPC_ERROR_MESSAGES.get(FSUIPCWrapper.FSUIPCResult.get(lastResult))});
       }
 }

//Add our listener to FSUIPC
fsuipc.addListener(fsuipcListener);

//Start continual processing, every 250 miliseconds
fsuipc.processRequests(250, true);

 .
 .
 .
 .

//Later, stop processing
fsuipc.cancelRequestsProcessing();
```

When we are finished, we should disconnect the FSUIPC (this will also release used resources).

`fsuipc.disconnect();`

## Bugs?

Well, it is most likely that some have found their way in the SDK. If you find any, I will be happy if you let me know. See the Contact chapter. Because honestly, this is “not a simple project”, at least not for me. It is not a hard hard, but neither simple. I am a seasonal C++ programmer, the times C++ was my second language are gone for a while. JNI (Java Native Interface), well, I did not read the whole documentation either. So what I was afraid of were memory leaks. Because, to be honest, the way this works is a little bit crazy so to speak.

How is it? Well, when you call **FSUIPC_Read** or **FSUIPC_Write** function from the FSUIPC C/C++ SDK to read/write to/from some FSUIPC offset, it will store that request to a memory. You can call these functions numerous times, the requests will get stores, but you will get no values (will not write any to the sim), until you call **FSUIPC_Process** function. That will trigger the inter process communication, send the stored data requests to the FSUIPC library running within the simulator, which will perform the reading and writing of required data, and send back results (actually, is stores them in the memory at place as indicated by the data requests that were send). Don’t get it? In other words, the FSUIPC_Process function will tell the FSUIPC library: „here are the data that we want to read/write, store the results to this address….“. This is still the C/C++ FSUIPC SDK, where is the Java part you ask?

Well, the FSUIPCWarpper.java class implements some native methods. These reflect the FSUIPC_Read, FSUIPC_Write, FSUIPC_Process functions (and some others). In a way, we are exchanging data here between Java and C/C++ code. That is, what Java JNI (Java Native Interface is for). And this is the side from FSUIPC C/C++ SDK to Java code. The problem is how to keep reference to the Java variable after the native function call ended… to be able to return values after process function. Because, we cannot know when developer will call it. Java has no pointers as C/C++ and that is, I believe, why the original SDK called FSUIPC_Process in each call of the FSUIPC_read and
FSUIPC_write implementation within the FSUIPCWrapper class (otherwise, it would lead to memory exception). I modified the libraries so that these functions does not do that and they behave like the FSUIPC SDK C/C++ read and write functions. For this to work, the library must remember the memory addresses where FSUIPC stores results for each request made, and also memory that acts as inter-changer between C/C++ and respective Java variable. Have no clue what I am writing about? Ok, no problem. In other words, we are doing quite a things with memory here ?? Not that it would that complex at the end of the day. If you look at the code, it is pretty simple. But, as I wrote before, I am not a JNI expert, so the risk of unintentional memory leak is there.

But, I tested both versions of the libraries (**fsuipc_java32.dll** and **fsuipc_java64.dll**) with the software called [C++ Memory Validator](https://www.softwareverify.com/cpp-memory.php), which is designed to find such as issues. Both times, the sample **FSUIPC Sim Monitor** app monitored the sim (Prepar 3D) for about an hour. It is an excellent program by the way. But why I am writing about it. The result indicate that there were about 980-1800 bytes (28 potential memory leaks) of unreleased memory. These were all originated from 3rd party source files, and I think are potential, because I was only monitoring the said libraries, so the freeing of memory indicated as these memory leaks simply may not have been recorded. If you want, you can examine the reports. The exports are stored in the **C++ Memory Validator Reports** folder.

## Contact

If you want to contact me in regard of this JAVA FSUIPC SDK, you can do so via an email: [admin@mouseviator.com](admin@mouseviator.com), or leave a post in the forums at [https://forums.mouseviator.com](https://forums.mouseviator.com), or here via github :). I will gladly hear from you any suggestions, bug reports etc.

## Thanks

Many people behind the flight simulator platforms we use, Pete and John Dowson for FSUIPC, Mark Burton and Paul Henty for their SDK, as it was great starting point for me.


## Change log

27.8.2021

The oficial version number is now: 1.0.2

* Added: The [ValueRetrieveMethod](https://www.mouseviator.com/wp-content/uploads/documents/fsuipc_java_sdk_javadoc/com/mouseviator/fsuipc/datarequest/primitives/StringRequest.ValueRetrieveMethod.html) enumeration to the [StringRequest](https://www.mouseviator.com/wp-content/uploads/documents/fsuipc_java_sdk_javadoc/com/mouseviator/fsuipc/datarequest/primitives/StringRequest.html) class, the variable and the respective get and set methods for this enumeration. It now drives how the underlying byte buffer will be converted to string. Either: [TO_FIRST_ZERO_BYTE](https://www.mouseviator.com/wp-content/uploads/documents/fsuipc_java_sdk_javadoc/com/mouseviator/fsuipc/datarequest/primitives/StringRequest.ValueRetrieveMethod.html#TO_FIRST_ZERO_BYTE) - will use the added **getZeroTerminatedString** function, or: [WHOLE_BUFFER](https://www.mouseviator.com/wp-content/uploads/documents/fsuipc_java_sdk_javadoc/com/mouseviator/fsuipc/datarequest/primitives/StringRequest.ValueRetrieveMethod.html#WHOLE_BUFFER) - which will return the content of the whole buffer. By default, the **StringRequest** class uses **TO_FIRST_ZERO_BYTE**. You can change this using the new [setValueRetrieveMethod(ValueRetrieveMethod valueRetrieveMethod)](https://www.mouseviator.com/wp-content/uploads/documents/fsuipc_java_sdk_javadoc/com/mouseviator/fsuipc/datarequest/primitives/StringRequest.html#setValueRetrieveMethod(com.mouseviator.fsuipc.datarequest.primitives.StringRequest.ValueRetrieveMethod)) function, or by using [getValue(ValueRetrieveMethod valueRetrieveMethod)](https://www.mouseviator.com/wp-content/uploads/documents/fsuipc_java_sdk_javadoc/com/mouseviator/fsuipc/datarequest/primitives/StringRequest.html#getValue(com.mouseviator.fsuipc.datarequest.primitives.StringRequest.ValueRetrieveMethod)), which will not change the stored behavior of the class.
* Added: The [getZeroTerminatedString(Charset charset)](https://www.mouseviator.com/wp-content/uploads/documents/fsuipc_java_sdk_javadoc/com/mouseviator/fsuipc/datarequest/DataRequest.html#getZeroTerminatedString(java.nio.charset.Charset)) function to [DataRequest](https://www.mouseviator.com/wp-content/uploads/documents/fsuipc_java_sdk_javadoc/com/mouseviator/fsuipc/datarequest/DataRequest.html) class that will return the contents of the underlying byte buffer up to first zero byte converted to String. 
* Changed: Modified build.xml so for the Netbeans project, so it now generates jar files with version number.
* Updates: The respective archives and folders with ready to distributon (compiled) files.

3.1.2021

* Added: Some Junit tests.
* Added: Added the LuaHelper, MacroHelper and LVarHelper classes.
* Added: The FSUIPCLVarLuaMacroTest.java into the FSUIPCSimpleTest project to show the usage of the added classes and to test them.
* Added: isConnected function to the FSUIPC class.
* Fixed: Constructors of primitive data request that takes initial value (constructs WRITE data request) always ended with Exception due to missing command.

19.1.2021

* Some internal changes and fixes in the FSUIPC class. For example, it was not possible to start processing requests again after disconnecting/connecting again. Some cleanup was not being done.

25.9.2020

* First release.


