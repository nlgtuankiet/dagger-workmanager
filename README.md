# Dagger 2 setup with WorkManager, a complete step by step guide

https://medium.com/@nlg.tuan.kiet/dagger-2-setup-with-workmanager-a-complete-step-by-step-guild-bb9f474bde37


### Setup guide:
See some red objects in the IDE along with the warning message
"Unresolved reference: ..."  
-> You might want to run `./gradlew :app:kaptDebugKotlin`, this will
make all generated code got generated

Got "permission denied: ./gradlew"?  
-> If you are on linux or macos, run `chmod +x ./gradlew` to add execute
permission to the `gradlew` file
