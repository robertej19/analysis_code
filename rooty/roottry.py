#!/usr/bin/python


import uproot
from icecream import ic
import numpy as np
import matplotlib.mlab as mlab
import matplotlib.pyplot as plt

ic.disable()

#filename = "converted_filtered_skim8_005032.root"
filename = "converted_filtered_processed.root"
#ff = ROOT.TFile(sys.argv[1])
#ff = ROOT.TFile(filename)

"""
tree.keys()
['nmb', 'Pp', 'Ppx', 'Ppy', 'Ppz', 'Ptheta', 'Pphi', 
'Pvx', 'Pvy', 'Pvz', 'Pvt', 'PSector', 'Pbeta', 'Pstat', 
'nml', 'Ep', 'Epx', 'Epy', 'Epz', 'Etheta', 'Ephi', 'Evx', 
'Evy', 'Evz', 'Evt', 'Ebeta', 'Estat', 'ESector', 'nmg', 
'Gp', 'Gpx', 'Gpy', 'Gpz', 'Gtheta', 'Gphi', 'Gvx', 'Gvy', 
'Gvz', 'Gvt', 'GSector', 'Gbeta', 'Gstat', 'beamQ', 
'liveTime', 'startTime', 'RFTime', 'helicity', 
'helicityRaw', 'EventNum', 'RunNum', 'Q2', 'Nu', 'q', 'qx',
'qy', 'qz', 'W2', 'xB', 't', 'combint', 'mPpx', 'mPpy',
'mPpz', 'mPp', 'mmP', 'meP', 'Mpx', 'Mpy', 'Mpz', 'Mp',
'mm', 'me', 'mGpx', 'mGpy', 'mGpz', 'mGp', 'mmG', 'meG',
'Pi0p', 'Pi0px', 'Pi0py', 'Pi0pz', 'Pi0theta',
'Pi0phi', 'Pi0M', 'Pi0Sector', 'pIndex', 'gIndex1',
'gIndex2', 'trento', 'trento2', 'trento3']  

"""


file = uproot.open(filename)


tree = file["T"]


q2 = tree["Q2"].array()
xB = tree["xB"].array()
t_mom = tree["t"].array()
trent1 = tree["trento"].array()
trent2 = tree["trento2"].array()
trent3 = tree["trento3"].array()
pi0M = tree['Pi0M'].array()

fff = open("myfile.txt","w")

filt_pi = []
#filt_trent = []
#filtering
for count,item in enumerate(pi0M):
#    print(item[0])
#    filt_trent.append(item[0])
    filt_pi.append(pi0M[count][0])
    fff.write("{},{},{},{}\n".format(q2[count],xB[count],t_mom[count][0],trent1[count][0]))

print("done filtering")


#arr = np.array(filt_trent)

print("number of events is:")
print(len(q2))

"""
# Binning:
t_bins = [0.09,0.15,0.2,0.3,0.4,0.6,1,1.5,2,3,4.5,6]
q2_bins = [1,1.5,2,2.5,3,3.5,4,4.5,5,5.5,6,7,8,9,12]
xB_bins = [0.1,0.15,0.2,0.25,0.3,0.35,0.4,0.45,0.5,0.55,0.6,0.7,0.85,1]
phi_bins = [0,18,36,54,72,90,108,
            126,144,162,180,198,
            216,234,252,270,288,
            306,324,342,360]
"""

i = 0
#while i < 100:
    #print(trent1[i])
    #print(trent2[i])
    #print(trent3[i])
#    print(pi0M[i])
#    i +=1

plt.hist(filt_pi,60)
plt.show()
