package utils

import groovy.io.FileType

class LumiCalc {

    def CalcLumi(qFcup) {
        def N_a = 6.02214E23 //avogadro's number
        def l = 5 //length of target, in centimeters
        def rho = 0.07 //density of liquid hydrogen target, in g/cm^3
        def e = 1.602E-19 //charge of an electron, in Columbs

        def Q = qFcup*1E-9 //charge is in units of nC, need to convert to Coulombs

        def L = N_a*l*rho*Q/e

        return L //The luminosity that is returned has units of cm^-2

    }

}