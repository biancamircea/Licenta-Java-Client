# Client SDK pentru Java - Licență
În urma unui studiu teoretic a soluției populare [Unleash](https://www.getunleash.io/), scopul acestui SDK este de a crea un model educațional similar, adaptat implementării personalizate de server.
Am evidențiat funcționalitățile ce pot fi considerate un standard în domeniul comutării de funcționalități, care au rolul de a evalua starea unui feature toggle și de a obține payload-ul dinamic oferit la nivelul interfeței serverului
## Cum se utilizează?
### Pasul 1: Instalarea dependinței
```xml
<dependency>
  <groupId>ro.mta.sdk</groupId>
  <artifactId>toggle-system</artifactId>
  <version>Latest version here</version>
</dependency>
```
### Pasul 2: Definirea unei instanțe de client
```java
ToggleSystemConfig toggleSystemConfig = ToggleSystemConfig.builder()
            .toggleServerAPI("<server-api-url>")
            .apiKey("<api-key>")
            .appName("demo")
            .build();
ToggleSystemClient toggleSystem =
            new ToggleSystemClient(toggleSystemConfig);
```
### Pasul 3: Utilizarea stării într-un bloc condițional
Funcționalitatea de bază este cea de evaluare a stării unui comutator:
```java
if(toggleSystem.isEnabled("<nume-toggle>")) {
            // execute new version
            ...
        } else {
            // execute old version
            ...
        }
```
Dar putem utiliza și încărcătura utilă a acestuia pe care o putem actualiza din interfața web: 
```java
toggleSystem.getPayload("<nume-toggle>")
```
