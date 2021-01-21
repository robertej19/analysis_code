import pandas as pd
import numpy as np 
import matplotlib.pyplot as plt 
import random 




def t_phi_plotter(phi_vals,t_vals,xbq2_ranges):
    x = phi_vals
    y = t_vals
    

    xmin = 0
    ymin = 0
    ymax = 6
    xmax = 360

    x_bins = np.linspace(xmin, xmax, 12) 
    y_bins = np.linspace(ymin, ymax, 6) 
    
    fig, ax = plt.subplots(figsize =(10, 7)) 
    # Creating plot 

    plt.hist2d(x, y, bins =[x_bins, y_bins], range=[[xmin,xmax],[ymin,ymax]])# cmap = plt.cm.nipy_spectral) 
    plt.title('foo-xb-{}-{}-q2-{}-{}.png'.format(xbq2_ranges[0],xbq2_ranges[1],xbq2_ranges[2],xbq2_ranges[3]))
    
    # Adding color bar 
    #plt.colorbar() 

    ax.set_xlabel('Phi')  
    ax.set_ylabel('t')  
    
    # show plot 

    plt.tight_layout()  

    plt.savefig('pics/foo-xb-{}-{}-q2-{}-{}.png'.format(xbq2_ranges[0],xbq2_ranges[1],xbq2_ranges[2],xbq2_ranges[3]))
    plt.close()


data = pd.read_csv('afterfixes.txt', sep=",", header=None)
data.columns = ["run_num", "event_num", "num_in_fd", "num_in_cd","helicity","xB","Q2","t","Phi"]


xb_ranges = [0,0.1,0.2,0.3,0.4,0.5,0.65,1]
q2_ranges = [1,2.5,4,5,7.5,10]

print(len(xb_ranges))
print(len(q2_ranges))

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

        t_phi_plotter(x,y,ranges)


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