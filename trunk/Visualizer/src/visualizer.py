'''
Created on 07.08.2016

Class to visualize the data of csv files. Each csv-file has to have some specific characteristics in order to
use this visualization class. The first column of each csv-file is the column which will be mapped on the 
x-axis. All the parameters which are mentioned in the csv-file has to start with the prefix 'PARAM:'. The other
parameter values except the first column will be used to provide a short description about the algorithm, but 
actually they are not necessary to create the plots. If you you use more parameters than the one which is used 
for the first column all the values for a specific parameter should be the same (e.g. if we have the parameter
'PARAM:numTrees' then all the values of this column have to have the same value). This visualization algorithm
cannot handle csv-files in which more than one parameter varies the values.
All measures has to start with the prefix 'MEASURE:'. For each measure a single plot will be created. It is 
important that all the csv-files you want visualize have the same measures. 
You can use the method 'makeOverviewDataset(input,output)' to visualize some csv-files. As the input parameter 
you have to give an array of paths to the csv-files and as the output you have to give a folder in which the
final pdf will be saved. There are a couple more parameters with which you can adjust the visualization.
If you do not want to specify the paths of the csv-files manually you can use the method 
'visualizeExperiment(name)'. As the parameter you have to give the name of the folder which contains the 
experiment and as the parameter 'resultPath' you have to give the path to the folder which contains the
experiment folder (which is given by the name). A experiment folder contains multiple folders. Each folder 
contains multiple csv-files which will be visualized in one pdf-file. The pdf-file will be saved in the
corresponding folder. Furthermore you can use the method 'visualizeAllExperiments(resultsPath)' to visualize
multiple experiments. For the parameter you only have to give the path to the folder which contains all the
experiment-folders.

@author: Moritz Kulessa
'''

import os
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.backends.backend_pdf import PdfPages

'''
Visualizes all experiments in the folder which is given by the parameter.
'''
def visualizeAllExperiments(resultsPath = None):
    if resultsPath == None:
        resultsPath = "../../RandomDecisionTrees/results/visualization"
    
    for experimentName in os.listdir(resultsPath):
        visualizeExperiment(experimentName)
            
'''
Visualizes an experiment with the specific folder-name. Each experiment contain one or more folders
in which all the csv-files will be visualized in one pdf-file. All the csv-files in one folder 
have to have the same measures.
'''
def visualizeExperiment(name, resultsPath = None):
    if resultsPath == None:
        resultsPath = "../../RandomDecisionTrees/results/visualization"
    
    for experimentName in os.listdir(resultsPath):
        if name == experimentName:
            experimentPath = resultsPath + '/' + experimentName
            
            #Obtain datasets which are involved in this experiment
            datasetNames = []
            for datasetName in os.listdir(experimentPath):
                datasetNames.append(datasetName)
    
            for datasetName in datasetNames:
            
                #Obtain csv-files for dataset
                datasetPath = experimentPath + '/' + datasetName
                filePaths = []
                for fileName in os.listdir(datasetPath):
                    if fileName.endswith('.csv'):
                        filePaths.append(datasetPath + '/' + fileName)
                
                makeOverviewDataset(filePaths, datasetPath, datasetName)
            
    
'''
Visualizes the given csv-files in one pdf-file. The csv-files are given by their file-paths. Each csv-file have to 
have the same measures. The parameter output-path is the folder in which the pdf will be saved. Furthermore we have 
multiple parameters to adjust the pdf:

rowsPerPage            = The number of rows of plots per page (rowsPerPage * colsPerPage = number of plots per page)
colsPerPage            = The number of columns of plots per page (rowsPerPage * colsPerPage = number of plots per page)
fontSize               = The size of the font
legendCols             = The number of columns used in the legend
grayScale              = If grayScale is true then the pdf contains only grey colors. If grayScale is false then colors will be used
lineStyles             = The style of the lines which will be plotted in the pdf
descriptionsPerPage    = The number of descriptions per page

'''
def makeOverviewDataset(filePaths, outputPath, name='Unknown', rowsPerPage=4, colsPerPage=3, fontSize=8, legendCols=1, grayScale=False, lineStyles = [ "-s", '-o', '-*','-p', '-.', ':', '-1', '-8', '-s', '-x', '-h', '-+', '-d', '-v', '-*'], descriptionsPerPage = 4):
    with PdfPages(outputPath + '/OVERVIEW_' + name + '.pdf') as pdf:

        #One row for the legend
        rowsPerPage += 1

        #Parameters for this method
        plotsPerPage = colsPerPage * rowsPerPage

        #Get initial information about the measures from the first file
        content = pd.read_csv(filePaths[0])        
        measures = getMeasureNames(content)
        params = getParamNames(content)

        #Configure layout (A4 with subplots)
        figSizeX = 9
        figSizeY = 12
        plt.figure(figsize=(figSizeX, figSizeY))
        
        #Configure scale of the subplots
        wspace=0.3
        hspace=0.3
        plt.subplots_adjust(wspace=wspace, hspace=hspace)
        plt.rcParams.update({'font.size': fontSize})
        if grayScale :
            plt.style.use('grayscale')  
                
        for i in range(len(measures)):
            for j,filePath in enumerate(filePaths):
                #Get information
                content = pd.read_csv(filePath)
                params = getParamNames(content)
                fileName = getFileName(filePath)
                
                #Plot the content
                plt.subplot(rowsPerPage, colsPerPage, (i%(plotsPerPage-colsPerPage))+1)
                plt.plot(content[params[0]], content[measures[i]], lineStyles[j], label=fileName)
                
                #Set labels
                plt.xlabel(params[0].replace('PARAM:', ''))
                plt.ylabel(measures[i].replace('MEASURE:', ''))
            
            #If the page is full or all plots have been printed then place the legend and the title and close the page
            if ((i%(plotsPerPage-colsPerPage)+1) == (plotsPerPage-colsPerPage)) or i == len(measures)-1:
                plt.suptitle("OVERVIEW_" + name, fontsize=12)

                #The legend is always placed in the last row of the page
                distance = (hspace + 1) * (rowsPerPage - 1)
                plt.subplot(rowsPerPage, colsPerPage, 1)
                plt.legend(getFileNames(filePaths), loc = (0.0, -distance), ncol=legendCols)
                pdf.savefig()
                plt.close()
                
                #If more plots have to be printed then initialize the new page
                if i < len(measures)-1:
                    plt.figure(figsize=(figSizeX, figSizeY))
                    plt.subplots_adjust(wspace=wspace, hspace=hspace)
                    plt.rcParams.update({'font.size': fontSize})
                    if grayScale :
                        plt.style.use('grayscale')   
        
        
        addPagesWithParameterInformation(figSizeX, figSizeY, wspace, hspace, pdf, filePaths, descriptionsPerPage)

'''
Adds some information about the parameter-values of each csv-file and append this information as tables at the end
of the pdf.
'''       
def addPagesWithParameterInformation(figSizeX, figSizeY, wspace, hspace, pdf, filePaths, descriptionsPerPage):
    
    plt.figure(figsize=(figSizeX, figSizeY))
    
    for j, filePath in enumerate(filePaths):
        #Get information
        content = pd.read_csv(filePath)
        params = getParamNames(content)
        fileName = getFileName(filePath)
    
        #Creates the model-descriptions
        modelDescription = []
        for i in range(len(params)):
            modelDescription.append([])
        modelDescription[0].append(fileName)
        for i, param in enumerate(params):
            if i> 0:
                modelDescription[i].append(str(param.replace('PARAM:', '')) + '=' + str(content[param][0]))
    
        #Plots the model-description
        plt.subplot(descriptionsPerPage, 1, (j%descriptionsPerPage)+1)
        plt.axis('tight')
        plt.axis('off')
        plt.table(cellText=modelDescription,loc='center')
        
        #Creates a new page or finalizes the pdf
        if (j+1)%descriptionsPerPage == 0 or j == (len(filePaths)-1):
            pdf.savefig()
            plt.close()
            plt.figure(figsize=(figSizeX, figSizeY))


#Extracts the measure-names from the data       
def getMeasureNames(content):
    colNames = list(content.columns)
    measures = []
    
    for colName in colNames:
        if colName.startswith('MEASURE:'):
            measures.append(colName)
    
    return measures

#Extracts the parameter-names from the data
def getParamNames(content):
    colNames = list(content.columns)
    params = []
    
    for colName in colNames:
        if colName.startswith('PARAM:'):
            params.append(colName)
            
    return params            
            
#Returns the file-name of a path
def getFileName(filePath):
    split = filePath.split('/')
    return split[len(split)-1].split('.')[0]

#Returns the file-names of an array of paths
def getFileNames(filePaths):
    fileNames = []
    for filePath in filePaths:
        fileNames.append(getFileName(filePath))
    return fileNames


if __name__ == '__main__':
    
    visualizeAllExperiments();     
    
    #visualizeExperiment('SPARSE_EXPERIMENT', 'C:/my_files/Studium/Bachelorarbeit/test_workspace/RandomDecisionTrees/results/visualization')         
    
    #filePaths = []
    #filePaths.append('C:/my_files/Studium/Bachelorarbeit/test_workspace/RandomDecisionTrees/results/visualization/SPARSE_EXPERIMENT/MULTILABEL_ENRON/MULTILABEL_CHAIN_ENSEMBLE_predictType=LABEL_CHAIN.csv')
    #filePaths.append('C:/my_files/Studium/Bachelorarbeit/test_workspace/RandomDecisionTrees/results/visualization/SPARSE_EXPERIMENT/MULTILABEL_ENRON/SPARSE_MULTILABEL_CHAIN_ENSEMBLE_predictType=LABEL_CHAIN.csv')
    #filePaths.append('C:/my_files/Studium/Bachelorarbeit/test_workspace/RandomDecisionTrees/results/visualization/SPARSE_EXPERIMENT/MULTILABEL_ENRON/MULTILABEL_CHAIN_ENSEMBLE_predictType=PERCENTAGE_CHAIN.csv')
    #filePaths.append('C:/my_files/Studium/Bachelorarbeit/test_workspace/RandomDecisionTrees/results/visualization/SPARSE_EXPERIMENT/MULTILABEL_ENRON/SPARSE_MULTILABEL_CHAIN_ENSEMBLE_predictType=PERCENTAGE_CHAIN.csv')
    #makeOverviewDataset(filePaths, 'C:/my_files/Studium/Bachelorarbeit/test_workspace/RandomDecisionTrees/results/visualization/SPARSE_EXPERIMENT/MULTILABEL_ENRON/', 'result')
