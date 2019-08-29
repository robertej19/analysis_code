#!/usr/bin/python

import sys
sys.argv.append('-b')
import ROOT
import matplotlib.pyplot as plt
import numpy as np

histo_title = "5038_H_proton_DeltaBeta_momentum_S2"
hipo_histos = ROOT.TFile(sys.argv[1])
h0 = hipo_histos.Get(histo_title)

e = 10.6 #GeV
bins = 800
eperbin = e/bins

def fitter(hist,fit,params,mincut,iteration):
  print("params are {0}".format(params))
  mu = params[1]
  sigma = params[2]
  fit.SetParameter(1, mu)
  fit.SetParameter(2, sigma)
  fit.SetRange(mu - 3*sigma, mu + 3sigma)
  print("fit range is {} to {}".format(mu - 3*sigma, mu + 3*sigma))
  #print(fit.GetRange())
  hist.Fit(fit, 'QR')
  fit_params = [fit.GetParameter(i) for i in range(0,3)]

  c1 = ROOT.TCanvas('c1','c1',1100,800)
  hist.SetTitle("Projection from {0} GeV to {1} GeV".format(round(mincut*energy_conv,2),round(maxcut*energy_conv,2)))
  hist.Draw()
  c1.Print("../iters/protons_{}_{}.pdf".format(mincut,iteration))
  return fit_params

def fit_histo(histo,mincut,maxcut,energy_conv):
  h1 = histo.ProjectionY("Histogram",mincut,maxcut,"[cutg]")
  peaks = ROOT.TSpectrum(2*3)
  n_peaks = peaks.Search(h1,1,"new")

  params = (100,0,0.03)
  f1 = ROOT.TF1('f1', 'gaus',-0.03,0.03)
  #f1.SetParameter(1, h1.GetBinCenter(h1.GetMaximumBin()))
  #f1.SetParameter(2, h1.GetRMS())
  params = fitter(h1,f1,params,mincut,1)
  params = fitter(h1,f1,params,mincut,2)
  params = fitter(h1,f1,params,mincut,3)

  c1 = ROOT.TCanvas('c1','c1',1100,800)
  #c1.SetLogz()
  h1.SetTitle("Projection from {0} GeV to {1} GeV".format(round(mincut*energy_conv,2),round(maxcut*energy_conv,2)))
  h1.Draw("colz")
  c1.Print("../iters/protons_{0}_{1}.pdf".format(mincut,maxcut))
  return params

amps, means, sigmas = [], [], []

for i in range(0,3):
	params = fit_histo(h0,i*10,i*10+40,eperbin)
	amps.append(params[0])
	means.append(params[1])
	sigmas.append(params[2])

print(sigmas)

"""
print("sigmas")
print(sigmas)
print("means")
print(means)
print("amps")
print(amps)
x = np.arange(len(sigmas))*10*eperbin
print(x)
print(len(x))
print(len(sigmas))

#plt.plot(x,means)
#plt.show"""

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
