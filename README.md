# Take-Home Assignment: Chat Feature Integration

This task was developed with Android/Kotlin.

Although it is not a satisfactory and intended outcome, I have performed the requirements of three tasks.

Please understand that the initial progress was carried out with SendBird UIKit because I didn't understand the contents of the assignment, but I revised it back to Chat API, so I don't have enough satisfaction with the assignment.


## Environments
- Windows 11 Pro
- Android Studio Hedgehog | 2013.1.1
- Sendbird-chat SDK 4.15.4

## Installation

1. Clone the repository. [GITHUB](https://github.com/flutowoo/my-assignments.git)
2. Select 'app' Configuration
3. 'RUN'

## Reference
- Sendbird SDK
- Copilot
- Sendbird chat samples

## Tasks
1. Task 1: API Integration and Basic Chat Setup
    + Proceed with Sendbird Chat API Guide [Sendbird SDK Doc](https://sendbird.com/docs/chat/sdk/v4/android/overview)
2. Task 2: Enhancing the Chat Interface
    + Chat implementation is implemented using Sendbird's MessageCollection system
    + The layout of the data sent and received was constructed using the ViewType of the RecyclerView Adapter.
3. Task 3: Implement a Feature - Read Receipts or Typing Indicators
    + I chose the second option[Typing Indicators] here and implemented it.
    + This function was implemented using startType, displayTypeUsers supported by GroupChannel.

## Description

* As if I had used the Sendbird SDK for a short time, the above tasks could be implemented in three directions.
    * Sendbird UI Component with UIKit
    * Sendbird UI Component extends with UIKit
    * Sendbird Chat SDK
* I didn't mean it, but I implemented the above three methods in order
* In this project, Android architectures (MVP, MVVM, MVVI) were not implemented.
* There is also a structurally simple part, and it happened because I was chased by the deadline.
* The screens consists of the following four.
    * Login (MAIN)
    * Channel List
    * User List
    * Chatting
* For persistent data management, use Preference
