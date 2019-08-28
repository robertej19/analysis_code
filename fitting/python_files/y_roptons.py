#!/usr/bin/python

import sys
sys.argv.append('-b')
import ROOT
import matplotlib.pyplot as plt
import numpy as np

ff = ROOT.TFile(sys.argv[1])
#f1 = ROOT.TF1('f1', 'gaus(0)+pol0(3)', -0.15,.15)
#f1 = ROOT.TF1('f1', 'gaus',-0.1,0.1)
#print(ff)
#print(type(ff))

for kk in ff.GetListOfKeys():
  obj = kk.ReadObj()
  print(obj.__class__)
  print(obj.GetTitle())
  print(obj.GetNbinsX())
  print(obj.GetNbinsY())



h0 = ff.Get("5038_H_proton_DeltaBeta_momentum_S2")#.ProjectionY("cutg",100,140,"[cutg]")

def fitter(hist,mincut,maxcut):
	f1 = ROOT.TF1('f1', 'gaus',-0.1,0.1)
	h1 = h0.ProjectionY("cutg",mincut,maxcut,"[cutg]")
	h1.Fit(f1, 'R')
	qq = ROOT.TSpectrum(2*3)
	nfound = qq.Search(h1,1,"new")

	#print("param is:")
	amp = f1.GetParameter(0)
	mean = f1.GetParameter(1)
	sigma = f1.GetParameter(2)
	#print(f1.GetParameter())

	c1 = ROOT.TCanvas('c1','c1',1100,800)
	h1.Draw("colz")
	c1.Print("iters/protons_{0}_{1}.pdf".format(mincut,maxcut))
	return amp, mean, sigma

amps, means, sigmas = [], [], []

for i in range(0,10):
	amp, mean, sigma = fitter(h0,i*10,i*10+40)
	amps.append(amp)
	means.append(mean)
	sigmas.append(sigma)

print(sigmas)

x = np.arange(len(sigmas))
print(x)
print(len(x))
print(len(sigmas))

plt.plot(x,means)
plt.show







