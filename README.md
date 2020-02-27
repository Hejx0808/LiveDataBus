# LiveDataBus

LiveDataBus is an event bus based on LiveData.
It has lifecycle perceive ability and supports Sticky messages

## Reference in the project
Gradle:

root build.gradle
```
repositories {
    maven { url 'https://jitpack.io' }
}
```
module build.gradle
```
dependencies {
    implementation 'com.github.Hejx0808:LiveDataBus:v1.0'
}
```

## Instructions

create Message.class:
``` kotlin
class Message {...}
class Event {...}
...
```

get channel by message.class:
``` kotlin
val channelMsg = LiveDataBus.getChannel(Message::class.java)
val channelEvent = LiveDataBus.getChannel(Event::class.java)
...
```

observe:
```kotlin
val observer = Observer<Message> {}
channelMsg.observe(observer)
// channelMsg.observeSticky(observer)
// channelMsg.observeLifecycle(lifecycleOwner, observer)
// channelMsg.observeLifecycleSticky(lifecycleOwner, observer)
```

post messages:
``` kotlin
val message = Message()
...
channelMsg.postEvent(message)
channelMsg.postEventDelay(Message(), 1000)
```

remove observer:
```kotlin
channelMsg.removeObserve(observer)
```

## Proguard rules

```
-dontwarn com.hjx.livedatabus.**
-keep class com.hjx.livedatabus.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.arch.core.** { *; }
```

## Version

Version | Features
---|---
[v1.0](https://github.com/Hejx0808/LiveDataBus/releases/tag/v1.0) | base version