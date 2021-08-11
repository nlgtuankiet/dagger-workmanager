# Dagger 2 setup with WorkManager, a complete step by step guide

https://medium.com/@nlg.tuan.kiet/dagger-2-setup-with-workmanager-a-complete-step-by-step-guild-bb9f474bde37

### Changelog:
## 2021-08-11
- Upgrade outdated dependencies (WorkManager 2.6.0-rc, Dagger 2.38.1)
- Use Dagger native Assisted Inject instead of Square Assisted Inject
- Fix ```Could not instantiate HelloWorldWorker``` bug because WorkManager use AndroidX App Startup 

### Setup guide:
:computer: Run `./gradlew :app:installDebug` to install debug
application to connected device

:crystal_ball: See some red objects in the IDE along with the warning
message "Unresolved reference: ..."  
:bulb: You might want to run `./gradlew :app:kaptDebugKotlin`, this will
make all generated code got generated

:crystal_ball: Got "permission denied: ./gradlew"?  
:bulb: If you are on linux or macos, run `chmod +x ./gradlew` to add execute
permission to the `gradlew` file
