Broadcast [![Build Status](https://travis-ci.org/zachoooo/Broadcast.svg?branch=master)](https://travis-ci.org/zachoooo/Broadcast)
=========
A sponge plugin for sending automated messages to your server.

This plugin is designed to be a simple and lightweight plugin for sending information to your players. Whether it be new server updates, social media links, or shoutouts, you need a simple way to communicate with players.

### About

This free and open source software is licensed under GPL v3. You can read more about our license [here](https://github.com/zachoooo/Broadcast/blob/bleeding/LICENSE).

Want to help out? [Fork me on Github](https://github.com/zachoooo/Broadcast).

Donations of any kind are always appreciated. [Donate here!](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=GXWENBDK2YDYA)

## Installation

### Prerequisites
* Java 8
* SpongeVanilla or SpongeForge

### Installation Steps
1. Download Broadcast to your server.
2. Place the downloaded jar file in your mods folder.
3. Restart your server.
4. Done! Now you can move onto configuration.

## Configuration
**Default Config**
```
delay=60
messages=[
    {
        color=red
        text="Thank you for choosing Broadcast!"
    },
    {
        color=yellow
        text="This plugin was written by zachoooo."
    },
    {
        color=aqua
        text="Edit the configuration to put your own messages in."
    },
    {
        color=yellow
        extra=[
            {
                text="["
            },
            {
                clickEvent {
                    action="open_url"
                    value="http://www.github.com/zachoooo/Broadcast"
                }
                color=green
                text=Github
            }
        ]
        text="Fork me on "
    }
]
prefix {
    extra=[
        {
            color=yellow
            text=Broadcast
        },
        {
            text="] "
        }
    ]
    text="["
}
random=false
no-repeat=true

```

**delay**: The time in seconds between broadcasts being displayed

**prefix**: Some text displayed before a broadcast to indicate that the message is a broadcast.

**random**: Whether or not to display the messages randomly or in order.

**no-repeat**: If random is set to true, then don't repeat the same message twice.

You can use configuration formatting from here:
http://minecraft.gamepedia.com/Commands#Raw_JSON_text

There are some generators that you can use to help you format chat:
* https://www.minecraftjson.com/
* http://minecraft.tools/en/tellraw.php

## Building

### Prerequisites
* Java 8
* Git
* Gradle (Packaged with project)

**Note:** If you do not have Gradle installed then use ./gradlew for Unix systems or Git Bash and gradlew.bat for Windows systems in place of any 'gradle' command.

Run the following commands to build the project yourself.
 
```
git clone https://github.com/zachoooo/Broadcast.git
cd Broadcast
gradle build
```

The compiled jar will be located in `./build/libs`.

## Authors
* zachoooo