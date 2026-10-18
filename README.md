# Cybercraft Chips
[Download](https://github.com/vata-ot-nata/cybercraft-chips/latest)

Plugin from the CyberCraft paper-plugin series. Adds a cyberpunk chip that can store data.
To write data, insert the chip into the Server, click the button in the chat, and enter your text into the chat. To read data, click the other button in the chat.  
Don't forget to remove the chip from the server after use!  
Each chip is assigned its own ID.

## Visual Demonstration
(Clickable)  
[![Visual demonstration](https://i.ytimg.com/vi/hnsJJRrCJNY/hq720.jpg?sqp=-oaymwEnCNAFEJQDSFryq4qpAxkIARUAAIhCGAHYAQHiAQoIGBACGAY4AUAB&rs=AOn4CLCXiDQ1bPbyYdFxdrkR7HzHKqOZuQ)](https://youtu.be/hnsJJRrCJNY)  

## Installation

1. [Download the latest serverChips.jar](https://github.com/vata-ot-nata/cybercraft-implants/releases/latest) and place it in your `plugins` folder
2. Restart the server
3. Use commands below to place a Server and to get a chip.

## Commands

To place a Server, use this command:
`/createserver`  
To get a chip, use this command:
`/give @p minecraft:stone_button[custom_name='{"text":"Chip","color":"dark_gray","italic":false}']`

## Storage

Animations are stored in `config.yml` in `.\plugins\ServerChips`
```yaml
data:
  '0': '1245'
  '1': Hello
  '2': World
  '3': Lalalala
```
