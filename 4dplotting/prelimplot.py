import pandas as pd
import numpy as np 
import matplotlib.pyplot as plt 
import random 
import sys
import os, subprocess
from pdf2image import convert_from_path
import math
from icecream import ic
import shutil
from PIL import Image, ImageDraw, ImageFont


def t_phi_plotter(phi_vals,t_vals,xbq2_ranges,pics_dir):
    x = phi_vals
    y = t_vals
    

    xmin = 0
    ymin = 0
    ymax = 6
    xmax = 360

    x_bins = np.linspace(xmin, xmax, 36) 
    y_bins = np.linspace(ymin, ymax, 24) 
    
    fig, ax = plt.subplots(figsize =(10, 7)) 
    # Creating plot 
    
    

    plt.hist2d(x, y, bins =[x_bins, y_bins], range=[[xmin,xmax],[ymin,ymax]])# cmap = plt.cm.nipy_spectral) 
    
    #For equal scales everywhere
    #norm = plt.Normalize(0, 120)
    #plt.hist2d(x, y, bins =[x_bins, y_bins], norm=norm, range=[[xmin,xmax],[ymin,ymax]])# cmap = plt.cm.nipy_spectral) 
    

    xmin = str(xbq2_ranges[0])
    xmax = str(xbq2_ranges[1])
    q2min = str(xbq2_ranges[2])
    q2max = str(xbq2_ranges[3])

    if len(q2min) < 2:
        q2min = "0"+q2min
    if len(q2max) < 2:
        q2max = "0"+q2max

    plot_title = 't_vs_phi-xb-{}-{}-q2-{}-{}'.format(xmin,xmax,q2min,q2max)

    plt.title(plot_title)
    
    # Adding color bar 
    #plt.colorbar() 

    ax.set_xlabel('Phi')  
    ax.set_ylabel('t')  
    
    # show plot 

    plt.tight_layout()  

    plt.savefig(pics_dir + plot_title+".png")
    plt.close()


data_dir = "plottingfiles/"

data_lists = os.listdir(data_dir)

data = pd.DataFrame()

for datacount, ijk in enumerate(data_lists):
    ic(datacount)
    frame = pd.read_csv(data_dir+ijk, sep=",", header=None)
    data = data.append(frame)

#data = pd.read_csv('afterfixes.txt', sep=",", header=None)
#data.columns = ["run_num", "event_num", "num_in_fd", "num_in_cd","helicity","xB","Q2","t","Phi"]



data.columns = ["run_num", "event_num", "num_in_fd", "num_in_cd","helicity","xB","Q2","t","Phi","W","ThetaXPi","Diff_pX","Diff_pY","MM_EPX2","ME_EPGG","Pi_Mass"]




"""
from matplotlib.colors import LinearSegmentedColormap

data1 = 3 * np.random.random((10, 10))
data2 = 5 * np.random.random((10, 10))

colors = ['red', 'brown', 'yellow', 'green', 'blue']
cmap = LinearSegmentedColormap.from_list('name', colors)
norm = plt.Normalize(0, 5)

fig, axes = plt.subplots(ncols=2)
for ax, dat in zip(axes, [data1, data2]):
    im = ax.imshow(dat, cmap=cmap, norm=norm, interpolation='none')
    fig.colorbar(im, ax=ax, orientation='horizontal')
plt.show()
"""





# #Long xb q2
# xb_ranges = [0,0.05,0.15,0.25,0.35,0.45,0.55,0.65,0.8,1]
# q2_ranges = [0,1,2,3,4,5,7,8,9,10,12]

xb_ranges = [0,0.3,1]
q2_ranges = [1,5,12]

print(len(xb_ranges))
print(len(q2_ranges))


save_folder = "pics/"
if os.path.isdir(save_folder):
    print('removing previous database file')
    ## Try to remove tree; if failed show an error using try...except on screen
    try:
        shutil.rmtree(save_folder)
    except OSError as e:
        print ("Error: %s - %s." % (e.filename, e.strerror))
else:
    print(save_folder+" is not present, not deleteing")

subprocess.call(['mkdir','-p',save_folder])
print(save_folder+" is now present")


for q2_ind in range(1,len(q2_ranges)):
    q2_min = q2_ranges[q2_ind-1]
    q2_max = q2_ranges[q2_ind]
    for xb_ind in range(1,len(xb_ranges)):
        xb_min = xb_ranges[xb_ind-1]
        xb_max = xb_ranges[xb_ind]
        print(xb_min,xb_max,q2_min,q2_max)
        ranges = [xb_min,xb_max,q2_min,q2_max]
        data_xb = data[(data['xB']>xb_min) & (data['xB']<=xb_max) & (data['Q2']>q2_min) & (data['Q2']<=q2_max)]

        # Creating dataset 
        y = data_xb["t"]
        x = data_xb["Phi"] 

        t_phi_plotter(x,y,ranges,save_folder)







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

    if direction=='vertical':
        new_im = Image.new('RGB', (int(new_width+0), int(new_height+images[0].size[1]/2)), color=bg_color)

    
    """
    image = Image.open('Focal.png')
    width, height = image.size 

    draw = ImageDraw.Draw(image)

    text = 'https://devnote.in'
    textwidth, textheight = draw.textsize(text)

    margin = 10
    x = width - textwidth - margin
    y = height - textheight - margin

    draw.text((x, y), text)

    image.save('devnote.png')

    # optional parameters like optimize and quality
    image.save('optimized.png', optimize=True, quality=50)
    """



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



img_dir = "pics/"

images = img_from_pdf(img_dir)


print(len(images))
#print(images)
layers = []

num_ver_slices = len(q2_ranges)-1
num_hori_slices = len(xb_ranges)-1
#for i in range(0,int(len(images)/num_ver_slices)):
for i in range(0,num_hori_slices):
    #print("on step "+str(i))
    layer = list(reversed(images[i*num_ver_slices:i*num_ver_slices+num_ver_slices]))
    #print(layer)
    #list(reversed(array))
    layers.append(layer)

#print(layers[0])

horimg = []

for counter,layer in enumerate(layers):
    print("len of layers is {}".format(len(layer)))
    print("counter is {}".format(counter))
    print("On vertical layer {}".format(counter))
    #print(layer)
    imglay = append_images(layer, direction='vertical')
    horimg.append(imglay)


print("Joining images horizontally")
final = append_images(horimg, direction='horizontal')
final_name = "joined_pictures_{}.jpg".format(num_ver_slices)
final.save(final_name)
print("saved {}".format(final_name))



"""
import plotly.graph_objects as go

import numpy as np

x0 = np.random.randn(100)/5. + 0.5  # 5. enforces float division
y0 = np.random.randn(100)/5. + 0.5
x1 = np.random.rand(50)
y1 = np.random.rand(50) + 1.0

x = np.concatenate([x0, x1])
y = np.concatenate([y0, y1])

fig = go.Figure()

fig.add_trace(go.Scatter(
    x=x0,
    y=y0,
    mode='markers',
    showlegend=False,
    marker=dict(
        symbol='x',
        opacity=0.7,
        color='white',
        size=8,
        line=dict(width=1),
    )
))
fig.add_trace(go.Scatter(
    x=x1,
    y=y1,
    mode='markers',
    showlegend=False,
    marker=dict(
        symbol='circle',
        opacity=0.7,
        color='white',
        size=8,
        line=dict(width=1),
    )
))
fig.add_trace(go.Histogram2d(
    x=x,
    y=y,
    colorscale='YlGnBu',
    zmax=10,
    nbinsx=14,
    nbinsy=14,
    zauto=False,
))

fig.update_layout(
    xaxis=dict( ticks='', showgrid=False, zeroline=False, nticks=20 ),
    yaxis=dict( ticks='', showgrid=False, zeroline=False, nticks=20 ),
    autosize=False,
    height=550,
    width=550,
    hovermode='closest',

)

fig.show()
"""