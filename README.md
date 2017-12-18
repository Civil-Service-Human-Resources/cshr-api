Important: if your default Java version is 9 prepend each command with path to Java 8, like this: 
`JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk1.8.0_152.jdk/Contents/Home" `
### TEST
If your default Java version is 9:

`./mvnw test`

### Build specific module (vcm for example):
`./mvnw install -pl vcm -am`
