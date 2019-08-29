#sys.argv.append('-b')
import matplotlib.pyplot as plt
import numpy as np

def fitter(f1):
  amp,mu,sig = [f1[i] for i in range(0,3)]
  print(amp,mu,sig)
  return amp, mu, sig

f1 = [4,3,5,2,5,6,3,1]

a,b,c  = fitter(f1)
print(a)
