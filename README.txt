*******************************************************************************
*
*  A Machine Learning Method for the Prediction of Receptor
*  Activation in the Simulation of Synapses
*  Copyright (C) 2013 J. Montes, E. Gomez, A. Merchan-Perez, J. DeFelipe,
*                     J. M. Peña
*
*  This program is free software: you can redistribute it and/or modify
*  it under the terms of the GNU Lesser General Public License as published
*  by the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU Lesser General Public License for more details.
*
*  You should have received a copy of the GNU Lesser General Public License
*  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*
*******************************************************************************

DISCLAIMER:

This is a lab development, intended for use only in experiments and not
for full distribution. Familiarity with UNIX-like systems (Linux, Mac,
etc.) command line operation is required for its use. An improved, more
user-friendly version fot his software is in development.


*******************************************************************************

TOOL:

A Machine Learning Method for the Prediction of Receptor Activation in the
Simulation of Synapses

*******************************************************************************

AUTHORS:

- J. Montes
- E. Gomez
- A. Merchán-Perez
- J. DeFelipe
- J. M. Peña

*******************************************************************************

VERSION:

1.0 alpha (pre-release)

*******************************************************************************

DESCRIPTION:

This is an implmentation of our machine-learning-based AMPA receptor
activation prediction model.

*******************************************************************************

SYSTEM REQUIREMENTS:

- UNIX-like command line environment (Linux, MacOS X or similar).
  Windows is not directly supported. This software could be executed
  in Windows using cygwin, or other tool capable of creating a
  Linux-like environment.
- Java 1.6 or higher.
- The R statistical tool (http://www.r-project.org/). This is used during
  the curve-fitting process. Previous verisons of this software used MATLAB
  for this task, but we have replace it with R, which produces the same result
  with improved performance. In adittion, R is free, like the rest of this
  program requirements.

*******************************************************************************

COMPONENTS:

- ML-AMPA.sh: This is the main program file. It is a bash
  shell script that performs the basic curve prediction tasks.
- AMPA.O_model_M5P.bin: This is the machine-learning model. It has been
  previously trained using a synapse dataset including 1000 different
  synapse configurations.
- weka.jar: The machine learning libary.
- src and bin directories: They contain the Java sorice code and binary files
  of the AMPA receptor activation prediction model.

*******************************************************************************

CONFIGURATION:

Before using this software, it has to be properly configured. To do so,
the ML-AMPA.sh file must be edited. More specifically, the R_HOME variable
inside this script has to be correctly set to the system path where R is
installed. Without R the program cannot perform the final curve-fitting stage
of the AMPA receptor activation prediction.

*******************************************************************************

USAGE:

To user this software, just change into the directory where the component files
are and run the ML-AMPA.sh script. This script requires a set of 5 arguments to
operate. These are the values of the synapse parameters:

- [AMPA]: AMPA concentration, in molecues per square micron.
- [T]   : Transporter concentration, in molecues per square micron.
- Ls    : Synapse length, in nm.
- Hc    : Synapse height, in nm.
- E     : Side of total apposition lenght, relative factor to Ls

Fo example, running the following command:

$ ./ML-AMPA.sh 2000 1600 500 16 1.5

Would predict the AMPA receptor activation curve of a syanpse with 2000 AMPA
receptors per square micrion, 1600 transporters per square micron, 500 nm of
synaptic length, 16 nm of synaptic height and a total apposition lenght of 1.5
times Ls, that is 750 nm in total.

Running this script will generate a csv file containing the predicted AMPA
activation curve, sampled in 0.05 ms intervals. The results file is called 
result.csv.

*******************************************************************************
*******************************************************************************
