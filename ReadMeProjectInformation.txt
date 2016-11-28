
1.	clone this project from "https://github.com/gillmylady/Master_Project.git"

2.	description of this folder:
			a.	src folder includes the source codes of this project
			b.	exp_data folder includes the experiment data of this project
			c.	instances folder includes all instances from referred paper, used in this project
			d.	Result.txt contains the experiment result from referred paper, used to compare data

3.	description of source codes:
			a.	there are two packages, master.project.main and master.project.problem
			b.	master.project.problem:
						i)	AbcBasicAlgorithm.java 
									--class file contains the ABC algorithm with parameters (arguments)
										changing arguments generate some mutant algorithms 
						ii)	conflictTest.java
									--used to test if there is conflict of scheduled tasks
											any task can only be scheduled at most once
											any task must be executed in specific time window
						iii)	instance.java
									--parse the instance file, save the data and use to run experiments
						iv)	logFile.java
									--log record, for our check
						v)	ReferredResult.java
									--parse Result file from referred paper, used to compare data
						vi)	ResultAnalysis.java
									--analize the data and see how many same results, how much we improved 
						vii)	RouletteWheel.java
									--probability selection class
						viii)	solution.java
									--generate solutions, local search and other heuristics are implemented here
						ix)	task.java
									--task class
						x)	technician.java
									--technician class, scheduled tasks of each technician are stored in here
			c.	master.project.main:
						i)	masterProject.java
									--main entrance of this project, different methods indicate different approach
											of running experiments.
						ii)	PublicData.java
									--static data here, 
											notice that the path of instances and Result must be modified as that you use
													in your computer
													
													
			


