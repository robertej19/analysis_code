#!/usr/bin/python

import sys
import os
sys.argv.append('-b')
import ROOT
from ROOT import gStyle
from ROOT import gROOT
from ROOT import TStyle
import matplotlib.pyplot as plt
import numpy as np
from ROOT import TCanvas, TGraph
from ROOT import TF1


def plotdistributer(type,zz,zzz):
	arx = makeplot(type,"LogOFF",zz,zzz)
	if type[4]:
		arx = makeplot(type,"LogON",zz,zzz)
	return arx

def makeplot(type,logtitle,zz,zzz):

	hist_title = type[0]

	#Title comprehension. Should be built in a separte function.
	split1 = hist_title.split("Phi")[1]
	splits = split1.split("<")
	xl = splits[0]
	group1 = (splits[2]).split("_")
	xh = group1[0]
	ql = group1[1]
	group2 = (splits[4]).split(" ")
	qh = group2[1]
	tl = group2[2]
	th = splits[6]

	h1 = ff.Get(hist_title)

	c1 = ROOT.TCanvas('c1','c1',120,100)

	h1.Draw("colz")

	#Use a custom function (altough the build in pol2 would also work)
	func = TF1('func', '[0] + [1]*cos(x*3.14159/180) + [2]*cos(2*x*3.14159/180)', 0, 360)
	fit = h1.Fit('func', 'QR')
	fit_params = [func.GetParameter(i) for i in range(0,3)]
	fp = fit_params
	#print("parameter list is: {}".format(fit_params))

	c1.Draw()

	h1.Draw("colz")

	#gStyle.SetOptStat(0)
	h1.SetTitle(type[1])
	h1.SetXTitle(type[2])
	h1.SetYTitle(type[3])
	h1.SetLineWidth(5)

	c1.Draw()
	#c1.Print("plots/{}/original_python_pdfs/{}_{}.pdf".format(zzz,logtitle,type[0]))
	arraystr = [xl, xh, ql, qh, tl, th, fp[0], fp[1], fp[2]]
	array = [float(i) for i in arraystr]
	return array


ff = ROOT.TFile(sys.argv[1])
qq = sys.argv[1]
ww = qq.split("/")
zz = ww[-1]
#print(ff.GetListOfKeys())
#print(zz)
xxx = zz.split(".")
zzz = xxx[0]
#print(zzz)

#os.mkdir("plots/"+zzz)
#os.mkdir("plots/"+zzz+"/original_python_pdfs")


xmins = []
qmins = []
tlists = []
p1s = []
p2s = []
p3s = []

xmin = float(sys.argv[2])
qmin = float(sys.argv[3])

for kk in ff.GetListOfKeys():
	obj = kk.ReadObj()
	title = obj.GetName()
	if "Ultra_Phi" in title:
		if obj.GetEntries()>10 and obj.GetMaximum()>10:
			histTitle = title
			type9 = (title,histTitle,"Phi","Counts",0,0,0,0,0,0)
			#print(obj.GetEntries())
			array = plotdistributer(type9,zz,zzz)
			#if (array[0]==xmin) and (array[2]==qmin):
				#smalist = [array[4],array[6],array[7],array[8]]
				#tlists.append(smalist)
			xmins.append(array[0])
			qmins.append(array[2])
			tlists.append(array[4])
			p1s.append(array[6])
			p2s.append(array[7])
			p3s.append(array[8])

avaliablex = list(set(xmins))
avaliableq = list(set(qmins))

print(avaliableq)
print(avaliablex)

for xb in avaliablex:
	for q2 in avaliableq:
		tvals = []
		p1vals = []
		p2vals = []
		p3vals = []
		for index,xval in enumerate(xmins):
			qval = qmins[index]
			if (xval==xb) and (qval==q2):
				tvals.append(tlists[index])
				p1vals.append(p1s[index])
				p2vals.append(p2s[index])
				p3vals.append(p3s[index])

		fig, ax = plt.subplots(1)#figure()
		#fig.autofmt_xdate()
		plt.plot(tlists,p1s,'+', markersize=12)
		plt.plot(tlists,p2s,'o', markersize=12)
		plt.plot(tlists,p3s,'x', markersize=12)
		axes = plt.gca()
		axes.set_xlim([0,1])
		axes.set_ylim([-40,50])
		fig.suptitle('Fits of Phi Dist. vs. t [xb={}-{},q2={}-{}]'.format(xb,xb+0.1,q2,q2+0.5), fontsize=20)
		plt.xlabel('t', fontsize=18)
		plt.ylabel('Fit parameter values (structure functions)', fontsize=16)

		fig.savefig('plots/test_xb-{}_q2-{}.pdf'.format(xb,q2))


		"""
		fig, ax = plt.subplots(1)#figure()
		#fig.autofmt_xdate()
		plt.plot(tlists,p1s,'+', markersize=12)
		plt.plot(tlists,p2s,'o', markersize=12)
		plt.plot(tlists,p3s,'x', markersize=12)
		axes = plt.gca()
		axes.set_xlim([0,1])
		axes.set_ylim([-40,50])
		fig.suptitle('Fits of Phi Dist. vs. t [xb={}-{},q2={}-{}]'.format(xb,xb+0.1,q2,q2+0.5), fontsize=20)
		plt.xlabel('t', fontsize=18)
		plt.ylabel('Fit parameter values (structure functions)', fontsize=16)

		fig.savefig('plots/test_xb-{}_q2-{}.pdf'.format(xb,q2))
		"""
