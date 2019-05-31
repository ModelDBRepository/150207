#!/bin/bash
# *******************************************************************************
# *
# *  A Machine Learning Method for the Prediction of Receptor
# *  Activation in the Simulation of Synapses
# *  Copyright (C) 2013 J. Montes, E. Gomez, A. Merchan-Perez, J. DeFelipe,
# *                     J. M. Peña
# *
# *  This program is free software: you can redistribute it and/or modify
# *  it under the terms of the GNU Lesser General Public License as published
# *  by the Free Software Foundation, either version 3 of the License, or
# *  (at your option) any later version.
# *
# *  This program is distributed in the hope that it will be useful,
# *  but WITHOUT ANY WARRANTY; without even the implied warranty of
# *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# *  GNU Lesser General Public License for more details.
# *
# *  You should have received a copy of the GNU Lesser General Public License
# *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
# *
# *******************************************************************************

# CONFIGURATION VARS
# These must be properly for the program to work

# Directory path to the R instalation. This MUST be set prperly.
R_HOME=/opt/local/lib/R

# Directory path to the rJava extension. This can usually work with the default value.
RJAVA_PATH=${R_HOME}/library/rJava/jri/

# *******************************************************************************

if [ $# -lt 5 ]
then
    echo "use $0: <[AMPA]> <[T]> <Ls> <Hc> <E>"
    exit 1
fi

RJAVA_CLASSPATH=${RJAVA_PATH}/JRI.jar
export R_HOME

java -Djava.library.path=${RJAVA_PATH} -cp ./bin:./weka.jar:${RJAVA_CLASSPATH} Predictor AMPA.O_model_M5P_all.bin $1 $2 $3 $4 $5
