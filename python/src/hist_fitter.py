#!/usr/bin/python

import sys
#I don't think the below line does anything, please delete after confirming this file still works
#sys.argv.append('-b')
import argparse
import ROOT
import matplotlib.pyplot as plt
import numpy as np

#histo_title = "5038_H_proton_DeltaBeta_momentum_S2"
histo_title = "5040_Hist_deltaB_psec1_layer1"
hipo_histos = ROOT.TFile(sys.argv[1])
h0 = hipo_histos.Get(histo_title)


for key in hipo_histos.GetListOfKeys():
  obj = key.ReadObj()
  print(obj.GetName())
  print(obj.GetTitle())
  print(obj.GetNbinsX())
  print(obj.GetNbinsY())
  print(obj.GetEntries())


e = 10.6 #GeV
bins = 800
eperbin = e/bins

def fitter(hist,fit,params,itera,mincut):
  #print("params are {0}".format(params))
  mu = params[1]
  sigma = params[2]
  fit.SetParameter(1, mu)
  fit.SetParameter(2, sigma)
  fit.SetRange(mu - 3*sigma, mu + 3*sigma)
  print("fit range is {} to {}".format(mu - 3*sigma, mu + 3*sigma))
  #print(fit.GetRange())
  hist.Fit(fit, 'QR')
  #c1 = ROOT.TCanvas('c1','c1',1100,800)
  #c1.SetLogz()
  #hist.GetXaxis().SetRange(720,880)
  #hist.SetTitle("Projection from {0} GeV to {1} GeV".format(mincut,mincut+40))
  #h1.Draw("colz")
  #hist.Draw()
  #c1.Print("../iters/protons_{0}_{1}_{2}.pdf".format(mincut,mincut+40,itera))

  fit_params = [fit.GetParameter(i) for i in range(0,4)]
  return fit_params

def fit_histo(histo,mincut,maxcut,energy_conv):
  h1 = histo.ProjectionY("Histogram",mincut,maxcut,"[cutg]")
  peaks = ROOT.TSpectrum(2*3)
  n_peaks = peaks.Search(h1,1,"new")
  qqqq = peaks.GetPositionX();
  peaks_list_sq = []
  for iz in range(0,n_peaks):
	#print("PEAKS ARE FOUND AT: {}".format(qqqq[iz]))
	peaks_list_sq.append(qqqq[iz]*qqqq[iz])
	  #print("sPEAKS ARE FOUND AT: {}".format(n_peaks))

  params = (100,0,0.03,0)
  #print("N PEAKS IS: {}".format(n_peaks))
  if sorted(peaks_list_sq)[0]>0.00015:
	print("initial peak found was too large:{}".format(np.sqrt(sorted(peaks_list_sq)[0])))
	params = (100,0,np.sqrt(sorted(peaks_list_sq)[0])/6,0)
  elif n_peaks>1:
	print("number of peaks > 1, autoscaling sigma")
	print(sorted(peaks_list_sq))
	print(peaks_list_sq[1:])
        lists = [a for a in peaks_list_sq if (a > 0.001)]
	#print("PEAKS ARE FOUND AT: {}".format(qqqq[iz]))
	params = (100,0,np.sqrt(sorted(lists)[0])/6,0)


  f1 = ROOT.TF1('f1', 'gaus(0)+pol0(3)', -0.03,.03)

  #f1 = ROOT.TF1('f1', 'gaus',-0.03,0.03)
  #f1.SetParameter(1, h1.GetBinCenter(h1.GetMaximumBin()))
  #f1.SetParameter(2, h1.GetRMS())

  params_list = []

  for i in range(0,10):
    new_params = fitter(h1,f1,params,i,mincut)
    print(i)

    print("new params {}".format(new_params))




    print("params were{}".format(params))

    params_list.append(new_params)
    if abs(new_params[2]-params[2])<0.000001:
      print("breaking after {}".format(i))
      break
    if i>8:
      print("warning: did not converge")
      break

    params = new_params


  c1 = ROOT.TCanvas('c1','c1',1100,800)
  #c1.SetLogz()
  h1.GetXaxis().SetRange(700,900)
  h1.SetMaximum(1.2*(params[0]+params[3]))
  h1.SetTitle("Projection from {0} GeV to {1} GeV".format(round(mincut*energy_conv,2),round(maxcut*energy_conv,2)))
  h1.Draw("colz")
  if(maxcut<100):
    c1.Print("../iters/protons_0{0}_0{1}.pdf".format(mincut,maxcut))
  elif(mincut<100):
    c1.Print("../iters/protons_0{0}_{1}.pdf".format(mincut,maxcut))
  else:
    c1.Print("../iters/protons_{0}_{1}.pdf".format(mincut,maxcut))
  return params, params_list

amps, means, sigmas = [], [], []
superset = []
for i in range(4,50):
	print("on set {}".format(i))
	params, params_list = fit_histo(h0,i*10,i*10+40,eperbin)
	amps.append(params[0])
	means.append(params[1])
	sigmas.append(params[2])
	superset.append(params_list)

print(sigmas)

print(params_list)

#print("sigmas")
#print(sigmas)
#print("means")
#print(means)
#print("amps")
#print(amps)
x = np.arange(len(sigmas))*10*eperbin
#print(x)
#print(len(x))
#print(len(sigmas))

print("the length is {}".format(len(x)))
print("the values of means are {}".format(means))



fig, ax = plt.subplots(1)#figure()
#fig.autofmt_xdate()
plt.plot(x,sigmas,'+')
fig.suptitle('Fitted Standard Deviation vs. Energy', fontsize=20)
plt.xlabel('Starting Energy (GeV)', fontsize=18)
plt.ylabel('Std. Dev. (Beta)', fontsize=16)

fig.savefig('plots/test_{}_{}.pdf'.format("energy","sigmas"))
#plt.show()




#plt.plot(x,sigmas,'+')
#print("trying to show plot!")
##plt.show
#plt.savefig("temp.pdf")
"""
for kk in ff.GetListOfKeys():
  obj = kk.ReadObj()
  print(obj.__class__)
  print(obj.GetTitle())
  print(obj.GetNbinsX())
  print(obj.GetNbinsY())
  print("value of x bin is: ")
  print(obj.GetXaxis().FindBin(100))#obj.FindLastBinAbove()))

  	#pl = ROOT.TPaveLabel(.05,100,.95,200,"New Title","br")
  	#pl.Draw()
"""

	#TCanvas *c1 = new TCanvas("c1", "c1",1024,44,926,686);
 	#c1.gStyle->SetOptTitle(0);
	#ROOT.gStyle.SetOptTitle(0)
