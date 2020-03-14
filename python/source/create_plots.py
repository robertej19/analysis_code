#!/usr/bin/python

import sys
import os
sys.argv.append('-b')
import ROOT
from ROOT import gStyle
from ROOT import gROOT
from ROOT import TStyle

print(sys.argv[1])
ff = ROOT.TFile(sys.argv[1])
print(ff)
qq = sys.argv[1]
ww = qq.split("/")
zz = ww[-1]
print(ff.GetListOfKeys())
print(zz)
xxx = zz.split(".")
zzz = xxx[0]
print(xxx)
print(zzz)
typeX = int(sys.argv[2])
LogOn = int(sys.argv[3])

def plotdistributer(type,zz,zzz):
	makeplot(type,"LogOFF",zz,zzz)
	if type[4]:

		makeplot(type,"LogON",zz,zzz)

def makeplot(type,logtitle,zz,zzz):
	hist_title = type[0]
	h1 = ff.Get(hist_title)
	c1 = ROOT.TCanvas('c1','c1',120,100)
	if type[6] > 0:
		h1.GetXaxis().SetRange(type[5],type[6])
	h1.Draw("colz")
	if type[7] > 0:
		h1.SetAxisRange(0,type[7],"Y")
	if type[8] > 0:
		h2 = ff.Get(type[9])
		h2.SetLineColorAlpha(2,1)
		h2.Draw("SAME")
		h2.SetAxisRange(0,110000,"Y")
		logtitle += "_Double"

	#gStyle.SetOptStat(0)
	h1.SetTitle(type[1])
	h1.SetXTitle(type[2])
	h1.SetYTitle(type[3])
	h1.SetLineWidth(5)

	if logtitle == "LogON":
		c1.SetLogz()
	c1.Draw()
	c1.Print("plots/{}/original_python_pdfs/{}_{}.pdf".format(zzz,logtitle,type[0]))

"""
for kk in ff.GetListOfKeys():
  obj = kk.ReadObj()
  print(obj.GetName())
  print(obj.__class__)
  print(obj.GetTitle())
  print(obj.GetNbinsX())
  print(obj.GetNbinsY())
  #print("value of x bin is: ")
  #print(obj.GetXaxis().FindBin(100))#obj.FindLastBinAbove()))
#h1 = ff.Get("5038_H_proton_DeltaBeta_momentum_S2")#.ProjectionY("cutg",0,40,"[cutg]")#5038_Hist_deltaB_psec1_layer1")#.ProjectionY("cutg",0,40,"[cutg]")
"""
#h1 = ff.Get("5039_Hist_deltaB_psec1_layer1")#.ProjectionY("cutg",0,40,"[cutg]")

#type1 = "output_file_histos_Hist_xB_Q2"

"""FORMAT: Hist name, title, xaxis, yaxis,logON/LogOFF,xmin,xmax,ymax,1 = enable double plots,second histo name"""
type1 = ("output_file_histos_Hist_heleproTheta",
	"Proton vs. Electron Theta Angle","Proton Angle","Electron Angle",
	1,0,0,0,0,0)
type2 = ("output_file_histos_Hist_heleproThetaDVMP",
	"Proton vs. Electron Theta Angle, DVPP Candidates","Proton Angle","Electron Angle",
	1,0,0,0,0,0)
type3 = ("output_file_histos_Hist_heleTheta",
	"Electron Angle (Theta), with Proton Coincidence","Electron Angle","Counts",
	0,0,0,0,0,0)
type4 = ("output_file_histos_Hist_hproTheta","Proton Angle (Theta)","Proton Angle","Counts",
	0,0,0,110000,0,0)
type5 = ("output_file_histos_Hist_LeptHadAngle",
	"Angle Between Lepton and Hadron Planes","Angle","Counts",
	0,0,90,0,0,0)

type6 = ("output_file_histos_Hist_hproThetaCD",
	"Proton Angle (Theta) in CD (Status > 4000)","Angle","Counts",
	0,0,0,110000,0,0)

type7 = ("output_file_histos_Hist_hproThetaFD",
	"Proton Angle (Theta) in FD (2000 < Status < 4000)","Angle","Counts",
	0,0,0,110000,0,0)

Dtype8 = ("output_file_histos_Hist_hproThetaFD",
	"Proton Angle (Theta) in FD and in CD","Angle","Counts",
	0,0,0,110000,1,"output_file_histos_Hist_hproThetaCD")

#plots = [type1,type9]
plots = [type1,type2,type3,type4,type5,type6,type7,Dtype8]

os.mkdir("plots/"+zzz)
os.mkdir("plots/"+zzz+"/original_python_pdfs")

#Full xb range:
#xbRange = ["0.00", "0.10", "0.20", "0.30","0.40", "0.50", "0.60", "0.70", "0.80"]
#Full q2 range:
#q2Range = ["0.0","0.5","1.0","1.5","2.0","2.5","3.0", "3.5","4.0", "4.5","5.0", "5.5","6.0", "6.5", "7.0","7.5","8.0", "8.5"]
xbRange = ["0.10", "0.20", "0.30","0.40", "0.50", "0.60", "0.70", "0.80"]
q2Range = ["1.0","1.5","2.0","2.5","3.0", "3.5","4.0", "4.5","5.0", "5.5","6.0"]
tRange = ["0.09","0.15","0.2","0.3","0.4","0.6","1.0","1.5","2","5"]


for kk in ff.GetListOfKeys():
  obj = kk.ReadObj()
  title = obj.GetName())
  if "Ultra_Phi" in title:
	  type9 = (title,histTitle,"Phi","Counts",0,0,0,0,0,0)
	  print(title)
	  plotdistributer(type9,zz,zzz)


"""
for k in range(0,len(tRange)-1):
	for j in range(0,len(q2Range)-1) :
		for i in range(0,len(xbRange)-1):
			title = "output_file_histos_Hist_Ultra_Phi{} < xB < {}_ {} < q2 < {} {} < t < {}".format(xbRange[i],xbRange[i+1],q2Range[j],q2Range[j+1],tRange[k],tRange[k+1])
			histTitle = "Counts vs. Phi, {} < xB < {}_ {} < q2 < {} {} < t < {}".format(xbRange[i],xbRange[i+1],q2Range[j],q2Range[j+1],tRange[k],tRange[k+1])
			type9 = (title,histTitle,"Phi","Counts",0,0,0,0,0,0)
			print(title)
			plotdistributer(type9,zz,zzz)
"""
"""FORMAT: Hist name, title, xaxis, yaxis,logON/LogOFF,xmin,xmax,ymax,1 = enable double plots,second histo name"""

"""
for j in range(0,len(q2Range)-1) :
	for i in range(0,len(xbRange)-1):
		title = "output_file_histos_Hist_beta_T{} < xB < {}_ {} < q2 < {}".format(xbRange[i],xbRange[i+1],q2Range[j],q2Range[j+1])
		histTitle = "Counts vs. t, {} < xB < {}_ {} < q2 < {}".format(xbRange[i],xbRange[i+1],q2Range[j],q2Range[j+1])
		type9 = (title,histTitle,"t (GeV^2)","Counts",0,0,25,1000,0,0)
		print(title)
		plotdistributer(type9,zz,zzz)
"""
"""
for type in plots:
	plotdistributer(type,zz,zzz)
"""
