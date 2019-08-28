#!/usr/bin/python

import sys
sys.argv.append('-b')
import ROOT
import matplotlib.pyplot as plt
import numpy as np

ff = ROOT.TFile(sys.argv[1])

"""
for kk in ff.GetListOfKeys():
  obj = kk.ReadObj()
  print(obj.__class__)
  print(obj.GetTitle())
  print(obj.GetNbinsX())
  print(obj.GetNbinsY())
  print("value of x bin is: ")
  print(obj.GetXaxis().FindBin(100))#obj.FindLastBinAbove()))
"""
e = 10.6 #GeV
bins = 800
eperbin = e/bins

h0 = ff.Get("5038_H_proton_DeltaBeta_momentum_S2")

def fitter(hist,mincut,maxcut,energy_conv):
	f1 = ROOT.TF1('f1', 'gaus',-0.1,0.1)
	h1 = h0.ProjectionY("Histogram",mincut,maxcut,"[cutg]")
	h1.Fit(f1, 'R')
	qq = ROOT.TSpectrum(2*3)
	nfound = qq.Search(h1,1,"new")

	amp = f1.GetParameter(0)
	mean = f1.GetParameter(1)
	sigma = f1.GetParameter(2)

	c1 = ROOT.TCanvas('c1','c1',1100,800)
	#TCanvas *c1 = new TCanvas("c1", "c1",1024,44,926,686);
   	#c1.gStyle->SetOptTitle(0);
	#ROOT.gStyle.SetOptTitle(0)
	h1.SetTitle("Projection from {0} GeV to {1} GeV".format(round(mincut*energy_conv,2),round(maxcut*energy_conv,2)))
	h1.Draw("colz")


	#pl = ROOT.TPaveLabel(.05,100,.95,200,"New Title","br")
	#pl.Draw()
	c1.Print("../iters/protons_{0}_{1}.pdf".format(mincut,maxcut))
	return amp, mean, sigma

amps, means, sigmas = [], [], []

for i in range(0,20):
	amp, mean, sigma = fitter(h0,i*10,i*10+40,eperbin)
	amps.append(amp)
	means.append(mean)
	sigmas.append(sigma)

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
#plt.show
