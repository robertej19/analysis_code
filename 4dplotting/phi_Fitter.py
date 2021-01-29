# 1.) Necessary imports.    
import numpy as np
import matplotlib.pyplot as plt
from scipy.optimize import curve_fit

# 2.) Define fit function.
def fit_function(phi,A,B,C):
    #A + B*np.cos(2*phi) +C*np.cos(phi)
    rads = phi*np.pi/180
    #return (A * np.exp(-x/beta) + B * np.exp(-1.0 * (x - mu)**2 / (2 * sigma**2)))
    return A + B*np.cos(2*rads) + C*np.cos(rads)

#print(fit_function(45,1,0,1))

def getPhiFit(phi_vals,phi_title,plot_dir):
    xmin = 0
    xmax = 360
    print("fitting {}".format(phi_title))
    
    data = phi_vals
    bins_x = np.linspace(xmin, xmax, 20)
    data_entries, bins = np.histogram(data,bins=bins_x)

    print(data_entries)

    if (max(data_entries) == 0):
        print("No data in this plot, saving and returning 0")

        plt.text(150, 0, "No Data")

        plt.hist(phi_vals, bins =np.linspace(0, 360, 20), range=[0,360])# cmap = plt.cm.nipy_spectral)

        plot_title = plot_dir + phi_title+".png"
        plt.savefig(plot_title)
        plt.close()
        print("plot saved to {}".format(plot_title))
        
        return [0,0,0]
    else:
        binscenters = np.array([0.5 * (bins[i] + bins[i+1]) for i in range(len(bins)-1)])

        # 5.) Fit the function to the histogram data.
        popt, pcov = curve_fit(fit_function, xdata=binscenters, ydata=data_entries, p0=[2.0, 2, 0.3])
        #print(popt) #popt contains the values for A, B, C

        # 6.)
        # Generate enough x values to make the curves look smooth.
        xspace = np.linspace(0, xmax, 10000)

        # Plot the histogram and the fitted function.
        plt.bar(binscenters, data_entries, width=bins[1] - bins[0], color='navy', label=r'Histogram entries')
        plt.plot(xspace, fit_function(xspace, *popt), color='darkorange', linewidth=2.5, label=r'Fitted function')

        # Make the plot nicer.
        plt.xlim(xmin,xmax)
        plt.xlabel(r'x axis')
        plt.ylabel(r'Number of entries')
        plt.title(r'Exponential decay with gaussian peak')
        plt.legend(loc='best')

        fit_params = "A: {:2.2f}, B:{:2.2f}, C:{:2.2f}".format(popt[0],popt[1],popt[2])
        plt.text(150, max(data_entries)/1.3, fit_params)

        plot_title = plot_dir + phi_title+".png"
        plt.savefig(plot_title)
        plt.close()
        print("plot saved to {}".format(plot_title))

        return popt

# 3.) Generate exponential and gaussian data and histograms.

if (__name__ == "__main__"):
    phi_vals = [10,5,6,7,17,8,19,40,50,70,60,30,50,60,90,180,270,310,350,330,359,289,287,289,310,315,201,300,318]
    #phi_vals = []
    plot_dir = "./"
    phi_title = "test_phi_fit"
    getPhiFit(phi_vals,phi_title,plot_dir)

#plt.hist(phi_vals, bins =np.linspace(0, 360, 20), range=[0,360])# cmap = plt.cm.nipy_spectral) 
#plt.show()







