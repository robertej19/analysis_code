import sys
sys.argv.append('-b')
import os, subprocess
from pdf2image import convert_from_path
from PIL import Image
import math
import icecream as ic


def img_from_pdf(img_dir):
	image_files = []
	lists = os.listdir(img_dir)
	sort_list = sorted(lists)
	for img_file in sort_list:
		print("On file " + img_file)
		image1 = Image.open(img_dir+img_file)
		image_files.append(image1)

	return image_files



def append_images(images, direction='horizontal',
                  bg_color=(255,255,255), aligment='center'):
    """
    Appends images in horizontal/vertical direction.

    Args:
        images: List of PIL images
        direction: direction of concatenation, 'horizontal' or 'vertical'
        bg_color: Background color (default: white)
        aligment: alignment mode if images need padding;
           'left', 'right', 'top', 'bottom', or 'center'

    Returns:
        Concatenated image as a new PIL image object.
    """
    widths, heights = zip(*(i.size for i in images))

    if direction=='horizontal':
        new_width = sum(widths)
        new_height = max(heights)
    else:
        new_width = max(widths)
        new_height = sum(heights)

    new_im = Image.new('RGB', (new_width, new_height), color=bg_color)

    offset = 0
    for im in images:
        if direction=='horizontal':
            y = 0
            if aligment == 'center':
                y = int((new_height - im.size[1])/2)
            elif aligment == 'bottom':
                y = new_height - im.size[1]
            new_im.paste(im, (offset, y))
            offset += im.size[0]
        else:
            x = 0
            if aligment == 'center':
                x = int((new_width - im.size[0])/2)
            elif aligment == 'right':
                x = new_width - im.size[0]
            new_im.paste(im, (x, offset))
            offset += im.size[1]

    return new_im


def chunks(l, n):
	spits = (l[i:i+n] for i in range(0, len(l), n))
	return spits



img_dir = sys.argv[1]

images = img_from_pdf(img_dir)


print(len(images))
print(images)
layers = []

len_of_xb_minus_2 = 8-1
num_ver_slices = len_of_xb_minus_2
num_pics = 35
#for i in range(0,int(len(images)/num_ver_slices)):
for i in range(0,int(num_pics/len_of_xb_minus_2)):
    print("on step "+str(i))
    layer = list(reversed(images[i*num_ver_slices:i*num_ver_slices+num_ver_slices]))
    print(layer)
    #list(reversed(array))
    layers.append(layer)

#print(layers[0])

horimg = []

for counter,layer in enumerate(layers):
    print("counter is {}".format(counter))
    print("On vertical layer {}".format(counter))
    print(layer)
    imglay = append_images(layer, direction='vertical')
    horimg.append(imglay)


print("Joining images horizontally")
final = append_images(horimg, direction='horizontal')
final.save("joined_{}.jpg".format(num_ver_slices))
















"""
import matplotlib.pyplot as plt
from mpl_toolkits.axes_grid1 import ImageGrid
import numpy as np

fig, axes = plt.subplots(nrows=6, ncols=7)
fig.tight_layout() # Or equivalently,  "plt.tight_layout()"

#plt.figure(1)

for counter,image in enumerate(images):
	print(counter)
	plt.subplot(6,7,counter+1)
	plt.imshow(image)
	plt.axis('off')
"""

"""
plt.subplot(211)
plt.imshow(images[0])
plt.axis('off')

plt.subplot(212)
plt.imshow(images[1])
plt.axis('off')
"""

#plt.show()

#fig = plt.figure(figsize=(2., 2.))
#grid = ImageGrid(fig, 111,  # similar to subplot(111)
#                 nrows_ncols=(2, 2),  # creates 2x2 grid of axes
#                 axes_pad=0.01,  # pad between axes in inch.
#                 )

#for ax, im in zip(grid, [images[0], images[1], images[2], images[3]]):
    # Iterating over the grid returns the Axes.
#    ax.imshow(im)


#data = random.random((5,5))
#img = plt.imshow(images[0])
#img.set_cmap('hot')
#plt.axis('off')
#plt.savefig("test.png", bbox_inches='tight')

#plt.show()
