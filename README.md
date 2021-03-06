See An [Gurux](http://www.gurux.org/ "Gurux") for an overview.

Join the Gurux Community or follow [@Gurux](https://twitter.com/guruxorg "@Gurux") for project updates.

Open Source GXSMS media component, made by Gurux Ltd, is a part of GXMedias set of media components, which programming interfaces help you implement communication by chosen connection type. Our media components also support the following connection types: network.

For more info check out [Gurux](http://www.gurux.org/ "Gurux").

With Gurux.SMS component you can send Short Messages with your mobile phone.

We are updating documentation on Gurux web page. 

If you have problems you can ask your questions in Gurux [Forum](http://www.gurux.org/forum).

You can get source codes from http://www.github.com/gurux or if you use Maven add this to your POM-file:
```java
<dependency>
  <groupId>org.gurux</groupId>
  <artifactId>gurux.sms</artifactId>
  <version>1.0.3</version>
</dependency>
```

Note!
It's important to listen OnError event. Connection might break and it's the only way to get info from it. 

Simple example
=========================== 
Before use you must set following settings:
* PhoneNumber
* Pin code
* PortName
* BaudRate
* DataBits
* Parity
* StopBits

It is also good to add listener and start to listen following events.
* onError
* onReceived
* onMediaStateChange
* onTrace
* onPropertyChanged

```java

GXSMS cl = new GXSMS();
cl.setPhoneNumber("Phone Number");
cl.setPINCode("PIN Code");
cl.setPortName(gurux.serial.GXSerial.getPortNames()[0]);
cl.setBaudRate(9600);
cl.setDataBits(8);
cl.setParity(Parity.ODD);
cl.setStopBits(StopBits.ONE);
cl.open();


```

Data is send with send command:

```java
GXSMSMessage msg = new GXSMSMessage();
msg.setData("Hello World!");
msg.setNumber("Phone Number");
cl.send(msg);

```
In default mode received data is coming as asynchronously from OnReceived event.

Event listener is adding class that you want to use to listen media events and derive class from IGXMediaListener.

```java
//1. Add class that you want to use to listen media events and derive class from IGXMediaListener
*/
 Media listener.
*/
class GXMediaListener implements IGXMediaListener
{
    /** 
    Represents the method that will handle the error event of a Gurux component.
    @param sender The source of the event.
    @param ex An Exception object that contains the event data.
    */
    @Override
    void onError(Object sender, RuntimeException ex)
    {
    }
 
    /** 
     Media component sends received data through this method.
 
     @param sender The source of the event.
     @param e Event arguments.
    */
    @Override
    void onReceived(Object sender, ReceiveEventArgs e)
    {
 
    }
 
    /** 
     Media component sends notification, when its state changes.
     @param sender The source of the event.    
     @param e Event arguments.
    */
    @Override
    void onMediaStateChange(Object sender, MediaStateEventArgs e)
    {
 
    }
 
    /** 
     Called when the Media is sending or receiving data.
     @param sender
     @param e
     @see IGXMedia.Trace
    */
    @Override
    void onTrace(Object sender, TraceEventArgs e)
    {
 
    }
 
    // Summary:
    //  Event raised when a property is changed on a component.
    @Override
    void onPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
 
    }
} 

```

Listener is registered calling addListener method.
```java
cl.addListener(this);

```