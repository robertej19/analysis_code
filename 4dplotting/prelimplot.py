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

    x_bins = np.linspace(xmin, xmax, 72) 
    y_bins = np.linspace(ymin, ymax, 48) 
    
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

def just_phi_plotter(phi_vals,xbq2t_ranges,pics_dir):
    x = phi_vals

    xmin = 0
    xmax = 360

    x_bins = np.linspace(xmin, xmax, 20) 

    fig, ax = plt.subplots(figsize =(10, 7)) 
    # Creating plot 
    
    

    plt.hist(x, bins =x_bins, range=[xmin,xmax])# cmap = plt.cm.nipy_spectral) 
    
    #For equal scales everywhere
    #norm = plt.Normalize(0, 120)
    #plt.hist2d(x, y, bins =[x_bins, y_bins], norm=norm, range=[[xmin,xmax],[ymin,ymax]])# cmap = plt.cm.nipy_spectral) 
    

    xmin = str(xbq2t_ranges[0])
    xmax = str(xbq2t_ranges[1])
    q2min = str(xbq2t_ranges[2])
    q2max = str(xbq2t_ranges[3])
    tmax = str(xbq2t_ranges[4])
    tmin = str(xbq2t_ranges[5])
    

    if len(q2min) < 2:
        q2min = "0"+q2min
    if len(q2max) < 2:
        q2max = "0"+q2max

    plot_title = 't_vs_phi-xb-{}-{}-q2-{}-{}-t-{}-{}'.format(xmin,xmax,q2min,q2max,tmin,tmax)

    plt.title(plot_title)
    
    # Adding color bar 
    #plt.colorbar() 

    ax.set_xlabel('Phi')  
    ax.set_ylabel('counts')  
    
    # show plot 

    plt.tight_layout()  

    plt.savefig(pics_dir + plot_title+".png")
    plt.close()


datadir = "pickled_data/"
datafile = "skims-168.pkl"
data = pd.read_pickle(datadir+datafile)
ic(data.shape)


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



"""
# Binning:
t_bins = [0.09,0.15,0.2,0.3,0.4,0.6,1,1.5,2,3,4.5,6]
q2_bins = [1,1.5,2,2.5,3,3.5,4,4.5,5,5.5,6,7,8,9,12]
xB_bins = [0.1,0.15,0.2,0.25,0.3,0.35,0.4,0.45,0.5,0.55,0.6,0.7,0.85,1]
phi_bins = [0,18,36,54,72,90,108,
            126,144,162,180,198,
            216,234,252,270,288,
            306,324,342,360]
"""
#Official splits
xb_ranges = [0.1,0.15,0.2,0.25,0.3,0.35,0.4,0.45,0.5,0.55,0.6,0.7,0.85,1]
q2_ranges = [1.0,1.5,2.0,2.5,3.0,3.5,4.0,4.5,5.0,5.5,6.0,7.0,8.0,9.0,12.0]
t_ranges = [0.09,0.15,0.2,0.3,0.4,0.6,1,1.5,2,3,4.5,6]


#Long xb q2
#xb_ranges = [0,0.05,0.15,0.25,0.35,0.45,0.55,0.65,0.8,1]
#q2_ranges = [0,1,2,3,4,5,7,8,9,10,12]

# xb_ranges = [0,0.3,0.5,1]
# q2_ranges = [1,5,12]

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
    #shutil.rmtree(save_folder)
    #shutil.rmtree(save_folder)
else:
    print(save_folder+" is not present, not deleteing")

subprocess.call(['mkdir','-p',save_folder])
print(save_folder+" is now present")


#create bigpic
"""
xxx = data['xB']
yyy = data['Q2']

xmin = 0
xmax = 1
ymin = 0
ymax = 12

x_bins = np.linspace(xmin, xmax, 100) 
y_bins = np.linspace(ymin, ymax, 120) 

fig, ax = plt.subplots(figsize =(10, 7)) 
# Creating plot 

plt.hist2d(xxx, yyy, bins =[x_bins, y_bins], range=[[xmin,xmax],[ymin,ymax]])# cmap = plt.cm.nipy_spectral) 
plt.colorbar() 

plt.show()
#Fo

sys.exit()
"""



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



def append_images(images, xb_counter, direction='horizontal', 
                  bg_color=(255,255,255), aligment='center'):
    
    # Appends images in horizontal/vertical direction.

    # Args:
    #     images: List of PIL images
    #     direction: direction of concatenation, 'horizontal' or 'vertical'
    #     bg_color: Background color (default: white)
    #     aligment: alignment mode if images need padding;
    #        'left', 'right', 'top', 'bottom', or 'center'

    # Returns:
    #     Concatenated image as a new PIL image object.
    
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


    offset = 0
    for im_counter,im in enumerate(images):
        ic(im_counter)
        if direction=='horizontal':
            y = 0
            if aligment == 'center':
                y = int((new_height - im.size[1])/2)
            elif aligment == 'bottom':
                y = new_height - im.size[1]
            new_im.paste(im, (offset, y))
            offset += im.size[0]
        else:
            print("xb counter is {}".format(xb_counter))
            if xb_counter < 0:
            #here we create vertical strip of Q2 values
                offset += im.size[1]

                draw = ImageDraw.Draw(new_im)

                text = str(q2_ranges[im_counter+1])
                textwidth, textheight = images[0].size[0]/5, images[0].size[1]/5

                margin = 10
                #x = images[0].size[0] - textwidth - margin
                #y = images[0].size[1] - textheight - margin
                x = 0.8*int(images[0].size[0])
                ic(im_counter)
                y = 1*(int(images[0].size[1])*(len(q2_ranges)-(im_counter+2)))
                ic(y)
                ic(text)
                fonts_path = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'fonts')
                font = ImageFont.truetype(os.path.join(fonts_path, 'agane_bold.ttf'), 150)


                draw.text((x, y), text,(0,0,0),font=font)
            
            else:
                x = 0
                if aligment == 'center':
                    x = int((new_width - im.size[0])/2)
                elif aligment == 'right':
                    x = new_width - im.size[0]
                new_im.paste(im, (x, offset))
            offset += im.size[1]

    if (direction=='vertical') and (xb_counter > -1):
        #This is for the x-axis labels (xB)

        draw = ImageDraw.Draw(new_im)

        text = str(xb_ranges[xb_counter+1])
        textwidth, textheight = images[0].size[0]/5, images[0].size[1]/5

        margin = 10
        #x = images[0].size[0] - textwidth - margin
        #y = images[0].size[1] - textheight - margin
        x = 0.7*int(images[0].size[0])
        y = int(images[0].size[1])*(len(q2_ranges)-1)
        fonts_path = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'fonts')
        font = ImageFont.truetype(os.path.join(fonts_path, 'agane_bold.ttf'), 150)


        draw.text((x, y), text,(0,0,0),font=font)


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

#make vertical axis labels
imglay1 = append_images(layers[0], -1, direction='vertical')
horimg.append(imglay1)


for xb_counter,layer in enumerate(layers):
    print("len of layers is {}".format(len(layer)))
    print("counter is {}".format(xb_counter))
    print("On vertical layer {}".format(xb_counter))
    #print(layer)
    imglay = append_images(layer, xb_counter, direction='vertical')
    imglay.save("testing1.jpg")
    horimg.append(imglay)

print("Joining images horizontally")
final = append_images(horimg, 0,  direction='horizontal')
final_name = "joined_pictures_{}.jpg".format(num_ver_slices)
final.save(final_name,optimize=True, quality=100)
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