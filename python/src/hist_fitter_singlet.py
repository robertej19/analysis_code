#!/usr/bin/python

import sys
#I don't think the below line does anything, please delete after confirming this file still works
sys.argv.append('-b')
import argparse
import ROOT
import matplotlib.pyplot as plt
import numpy as np

#histo_title = "5038_H_proton_DeltaBeta_momentum_S2"
#histo_title = "output_file_histos_hist_hist_pion_mass_nocut_fd"
hipo_histos = ROOT.TFile(sys.argv[1])
#h0 = hipo_histos.Get(histo_title)


for key in hipo_histos.GetListOfKeys():
  obj = key.ReadObj()
  # print(obj.GetName())
  # print(obj.GetTitle())
  # print(obj.GetNbinsX())
  # print(obj.GetNbinsY())
  # print(obj.GetEntries())
  if "output_file_histos_hist_pion_mass_excut_fd" in obj.GetName():
  #if "output_file_histos_hist_missing_mass_squared_epX_zoomed_excut_fd" in obj.GetName():
    h0 = obj

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
  #h1 = histo.ProjectionY("Histogram",mincut,maxcut,"[cutg]")
  h1 = histo
  peaks = ROOT.TSpectrum(2*3)
  n_peaks = peaks.Search(h1,1,"new")
  peak_x_position = peaks.GetPositionX()
  peaks_list_sq = []

  print("number of peaks is {}".format(n_peaks))
  print("peak x position is {}".format(peak_x_position))
  # for iz in range(0,n_peaks):
	# #print("PEAKS ARE FOUND AT: {}".format(peak_x_position[iz]))
	#   peaks_list_sq.append(peak_x_position[iz]*peak_x_position[iz])
	#   #print("sPEAKS ARE FOUND AT: {}".format(n_peaks))

  # params = (h1.GetMaximum(),0,0.03,0)
  # #print("N PEAKS IS: {}".format(n_peaks))
  # # if sorted(peaks_list_sq)[0]>0.00015:
  # #   print("initial peak found was too large:{}".format(np.sqrt(sorted(peaks_list_sq)[0])))
  # # params = (100,0,np.sqrt(sorted(peaks_list_sq)[0])/6,0)
  # # elif n_peaks>1:
	# # print("number of peaks > 1, autoscaling sigma")
	# # print(sorted(peaks_list_sq))
	# # print(peaks_list_sq[1:])
  # lists = [a for a in peaks_list_sq if (a > 0.001)]
	# #print("PEAKS ARE FOUND AT: {}".format(peak_x_position[iz]))

	#params = (100,0,np.sqrt(sorted(lists)[0])/6,0)
  
  params = (4000, 140, 12, 0) #This is the estimate for pion mass
 # params = (4000, 0, 1, 0) #This is the estimate for missing mass squared


  f1 = ROOT.TF1('f1', 'gaus(0)+pol0(3)', -0.03,.03)

  #f1 = ROOT.TF1('f1', 'gaus',-0.03,0.03)
  #f1.SetParameter(1, h1.GetBinCenter(h1.GetMaximumBin()))
  #f1.SetParameter(2, h1.GetRMS())

  params_list = []

  for i in range(0,10):
    new_params = fitter(h1,f1,params,i,mincut)
    print(i)

    print("new params mean: {0} sigma: {1}".format(new_params[1],new_params[2]))

    #print("params were{}".format(params))

    params_list.append(new_params)
    if abs(new_params[2]-params[2])<0.001:
      print("breaking after {}".format(i))
      break
    if i>8:
      print("warning: did not converge")
      break

    params = new_params


  c1 = ROOT.TCanvas('c1','c1',1100,800)
  #c1.SetLogz()
  h1.GetXaxis().SetRange(50,300)
  #h1.SetMaximum(1.2*(params[0]+params[3]))
  #h1.SetTitle("Projection from {0} GeV to {1} GeV".format(round(mincut*energy_conv,2),round(maxcut*energy_conv,2)))
  h1.Draw("colz")
  c1.Print("examplehistogramfitout.pdf")
  #if(maxcut<100):
  #  c1.Print("../iters/protons_0{0}_0{1}.pdf".format(mincut,maxcut))
  #elif(mincut<100):
  #  c1.Print("../iters/protons_0{0}_{1}.pdf".format(mincut,maxcut))
  #else:
  #  c1.Print("../iters/protons_{0}_{1}.pdf".format(mincut,maxcut))
  return params, params_list



print(h0.GetName())
print(h0.GetMaximum())

fit_histo(h0,0,300,1)
