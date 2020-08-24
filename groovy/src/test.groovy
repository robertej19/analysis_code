def CalcLumi(qFcup) {
	N_A = 6.02214E23 //avogadro's number
	l = 5 //length of target, in centimeters
	rho = 0.07 //density of liquid hydrogen target, in g/cm^3
	e = 1.602E-19 //charge of an electron, in Columbs

	Q = qFcup*1E-9 //charge is in units of nC, need to convert to Coulombs

	L = N_A*l*rho*Q/e

	return L

}

println(CalcLumi(35000))