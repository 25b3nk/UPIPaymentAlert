# UPI Payment Alert

## To Do

- [x] Read SMS
- [x] Add TTS functionality
- [x] Permission request should be handled elegantly
- [x] Read SMS and announce the payment (RegEx)
- [x] Run the app in background
- [ ] Read SMS from filtered receivers only
- [ ] Use notification from UPI apps to validate the SMS read
- [ ] Use UPI reference number to validate the transaction before announcing


## References

1. [How to read SMS](https://stackoverflow.com/a/9494532/5258060)
1. [Adding TTS](https://www.tutorialspoint.com/android/android_text_to_speech.htm)
1. [Request permission](https://developer.android.com/training/permissions/requesting#manage-request-code-yourself)
1. [Regex for extracting amount from SMS](https://stackoverflow.com/a/37409435/5258060)
1. [Background services and intents blog](https://proandroiddev.com/deep-dive-into-android-services-4830b8c9a09)
1. [Creating notification](https://developer.android.com/training/notify-user/build-notification)
1. [To run the app in background, we need to have notification running](https://developer.android.com/guide/components/activities/background-starts)
1. [Tasks & back stack](https://developer.android.com/guide/components/activities/tasks-and-back-stack)
