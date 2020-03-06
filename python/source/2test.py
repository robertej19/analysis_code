

xbRange = ["0.07", "0.21", "0.36", "0.64", "0.79", "0.93"]
q2Range = ["1.5", "2.5", "3.5", "4.5", "5.5", "0.93"]

for j in range(0,len(q2Range)-1) :
	for i in range(0,len(xbRange)-1):
		title = "output_file_histos_Hist_beta_T{} < xB < {}_ {} < q2 < {}".format(xbRange[i],xbRange[i+1],q2Range[j],q2Range[j+1])
		print(title)
