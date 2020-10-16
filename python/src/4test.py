import array, numpy
import ROOT
from ROOT import gStyle
from ROOT import gROOT
from ROOT import TStyle

c1 = ROOT.TCanvas('c1','c1',120,100)

# Make two graphs with the same X ranges but different Y values.
x = array.array('d',range(10))
y1 = array.array('d',(i for i in x))
y2 = array.array('d',(-3*i-5 for i in x))

g1 = ROOT.TGraph(len(x),x,y1)
g2 = ROOT.TGraph(len(x),x,y2)
color2 = ROOT.kBlue
g2.SetLineColor(color2)
g2.SetMarkerColor(color2)
g2.SetLineStyle(2)

# Get the dynamic range of both graphs.
# Note: you cannot use GetMaximum() or GetMaximum()
# as these can contain magic codes like -1111 for autoscaling.
y1max = ROOT.TMath.MaxElement(g1.GetN(),g1.GetY())
y1min = ROOT.TMath.MinElement(g1.GetN(),g1.GetY())
y2max = ROOT.TMath.MaxElement(g2.GetN(),g2.GetY())
y2min = ROOT.TMath.MinElement(g2.GetN(),g2.GetY())

# Define a TF2 that takes a y2 value and scales it to a new
# y3 value that has the same dynamic range as y1.
params = [ ("y1max", y1max), ("y1min", y1min),
        ("y2max", y2max), ("y2min", y2min) ]
fscale = ROOT.TF2("fscale","(y-[3])*([1]-[0])/([3]-[2]) + [1]",y1min,y1max,y2min,y2max)
for i,(parname, parvar) in enumerate(params):
    fscale.SetParName(i,parname)
    fscale.SetParameter(parname,parvar)

# Make a new copy of g2 so that the original data are preserved.
g3 = g2.Clone("g3")
g3.Apply(fscale)

# Sanity check
y3max = ROOT.TMath.MaxElement(g3.GetN(),g3.GetY())
y3min = ROOT.TMath.MinElement(g3.GetN(),g3.GetY())
assert (y3max == y1max) and (y3min == y1min)

# Now draw only the first graph and the scaled one.
mg = ROOT.TMultiGraph("mg","mg")
mg.Add(g1)
mg.Add(g3)
mg.Draw("ALP")
mg.GetXaxis().SetTitle("g1")
ROOT.gPad.Modified()
ROOT.gPad.Update()

# Get the coordinates of the left-side Y axis.
uxmax, uymin, uymax = ROOT.gPad.GetUxmax(), ROOT.gPad.GetUymin(), ROOT.gPad.GetUymax()

# Define the inverse function of fscale
funscale = ROOT.TF2("funscale","(y-[0])*([3]-[2])/([1]-[0])+[2]",y1min,y1max,y3min,y3max)
for i,(parname, parvar) in enumerate(params):
    funscale.SetParName(i,parname)
    funscale.SetParameter(parname,parvar)
# Note: funscale(0,fscale(0,i)) == i should always be true for all real numbers i.

right_axis = ROOT.TGaxis(uxmax, uymin, uxmax, uymax,
                        funscale(0,uymin), funscale(0,uymax), 510, "+L" )
right_axis.SetTitle("g2")
right_axis.Draw()

c1.Print("testpng.pdf")
