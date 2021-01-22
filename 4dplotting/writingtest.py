import os
from PIL import Image, ImageDraw, ImageFont

image = Image.open('focal.png')
width, height = image.size 

draw = ImageDraw.Draw(image)

text = 'https://devnote.in'
textwidth, textheight = width/3, height/3

margin = 10
x = width - textwidth - margin
y = height - textheight - margin


fonts_path = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'fonts')
font = ImageFont.truetype(os.path.join(fonts_path, 'agane_bold.ttf'), 240)

draw.text((x, y), text,(0,0,0),font=font)



ImageDraw.Draw(image).text(
    (0, 0),  # Coordinates
    'Hello world!',  # Text
    (0, 0, 0)  # Color
)

image.save('devnote.png')

# optional parameters like optimize and quality
image.save('optimized.png', optimize=True, quality=50)