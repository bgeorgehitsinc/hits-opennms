#!/usr/bin/env python3

import glob
import json
import sys
import os
import shutil 
import re
import tempfile
from library import common
#from library import libgit as lg
from library import libyaml_v2


common_library=common.common()

working_directory=tempfile.TemporaryDirectory()

# We don't want to modify the main files, make a copy of the .circleci folder 
# into a working directory 
shutil.copytree(".circleci",os.path.join(working_directory.name,".circleci"))

main_filename="@main.yml"
path_to_main_folder=os.path.join(working_directory.name,".circleci","main")
path_to_main_yml=os.path.join(path_to_main_folder,main_filename)
path_to_modified_main=os.path.join(working_directory.name,".circleci",main_filename.replace("@",""))
 
path_to_executors_yml=os.path.join(path_to_main_folder,"executors.yml")
path_to_parameters_yml=os.path.join(path_to_main_folder,"parameters.yml")

path_to_pipeline_parameters=os.path.join("/tmp","pipeline-parameters.json")
pipeline_parameters=common_library.load_json(path_to_pipeline_parameters)


path_to_build_components=os.path.join("/tmp","build-triggers.json")
build_components=common_library.load_json(path_to_build_components)


alias_folder="aliases"
commands_folder="commands"
workflow_folder="workflows"
job_folder="jobs"
component_folders=[alias_folder,commands_folder,workflow_folder,job_folder]

components_path=os.path.join(working_directory.name,".circleci","main")

print("main_filename:",main_filename)
print("path_to_main:",path_to_main_yml)
print("path_to_modified_main:",path_to_modified_main)
print("components_path:",components_path)

if os.path.exists(os.path.join("/tmp",".circleci")):
    print("clean up existing folder:",os.path.join("/tmp",".circleci"))
    shutil.rmtree(os.path.join("/tmp",".circleci"))


# Read the @main.yml file
main_yml_content=common_library.read_file(path_to_main_yml)
keywords=common_library.extract_keywords(path_to_main_yml)

for keyword in keywords:
    for sub_keyword in keywords[keyword]:
        if "workflows" in keyword:
            continue
        tmp_page=sub_keyword.replace("#","").replace(keyword+":","") 
        if ".index" in tmp_page:
            keywords[keyword][sub_keyword]["commands"]=common_library.expand_index(sub_keyword,path_to_main_folder,[])
        else:
            keywords[keyword][sub_keyword]["commands"]=common_library.expand_keyword(sub_keyword,path_to_main_folder)

final_output=""
re_pattern="^.*#.*#"

for e in main_yml_content:
    re_match=re.match(re_pattern,e)
    if re_match:
        if "#workflows#" in re_match.group():
            libyaml=libyaml_v2.libyaml_v2()
            workflow_path=os.path.join(".circleci","main","workflows","workflows_v2.json")
            workflow_data=common_library.load_json(workflow_path)
            libyaml.json_setup(workflow_path)
            sample_workflow=[]
            level=0
            if "workflows:" not in sample_workflow:
                sample_workflow.append(libyaml.create_space(level)+"workflows:")
            level=level+2

            if not build_components["experimental"] and \
               (build_components["build"]["docs"] and build_components["build"]["ui"]) or \
               (build_components["build"]["docs"] and build_components["build"]["build"]) or \
               (build_components["build"]["ui"] and build_components["build"]["build"]):
               sample_workflow.append(libyaml.create_space(level)+"multibuild:")
            elif not build_components["experimental"] and \
               build_components["build"]["docs"] and \
               not build_components["build"]["ui"] and \
               not build_components["build"]["build"]:
               sample_workflow.append(libyaml.create_space(level)+"docs:")
            elif not build_components["experimental"] and \
               not build_components["build"]["docs"] and \
               build_components["build"]["ui"] and \
               not build_components["build"]["build"]:
               sample_workflow.append(libyaml.create_space(level)+"ui:")
            elif not build_components["experimental"] and \
               not build_components["build"]["docs"] and \
               not build_components["build"]["ui"] and \
               build_components["build"]["build"]:
               sample_workflow.append(libyaml.create_space(level)+"build:")
            else:
               sample_workflow.append(libyaml.create_space(level)+"autobuild:")

            level+=2
            sample_workflow.append(libyaml.create_space(level)+"jobs:")
            level+=2

            if build_components["rpm-packages"]["minion"] or \
               build_components["rpm-packages"]["horizon"] or \
               build_components["rpm-packages"]["sentinel"]:
                print("rpm-packages > all:",libyaml.tell_extended_requirements('rpms'))
                workflow=libyaml.generate_workflows(workflow_data,"rpms",level,sample_workflow,enable_filters=False)
                
                if len(sample_workflow)>1:
                    for e in workflow:
                        print("rpms","Looking at",e)
                        if e not in sample_workflow:
                            sample_workflow.append(e)
                else:
                    sample_workflow=workflow
            if build_components["tests"]["integration"]:
                build_components["build"]["build"]=True
                print("tests > integration:",libyaml.tell_extended_requirements('integration-test'))
                
                workflow=libyaml.generate_workflows(workflow_data,"integration-test",level,sample_workflow,enable_filters=False)
                if len(sample_workflow)>1:
                    for e in workflow:
                        print("integration","Looking at",e)
                        if e not in sample_workflow:
                            sample_workflow.append(e)
                else:
                    sample_workflow=workflow

            if build_components["tests"]["smoke"]:
                build_components["build"]["build"]=True
                print("tests > smoke:",libyaml.tell_extended_requirements('smoke'))
                workflow=libyaml.generate_workflows(workflow_data,"smoke",level,sample_workflow,enable_filters=False)
                if len(sample_workflow)>1:
                    for e in workflow:
                        print("smoke","Looking at",e)
                        if e not in sample_workflow:
                            sample_workflow.append(e)
                else:
                    sample_workflow=workflow

            if build_components["debian-packages"]["minion"] or \
               build_components["debian-packages"]["horizon"] or \
               build_components["debian-packages"]["sentinel"]:
                print("debian-packages > all:",libyaml.tell_extended_requirements('debs'))
                workflow=libyaml.generate_workflows(workflow_data,"debs",level,sample_workflow,enable_filters=False)
                if len(sample_workflow)>1:
                    for e in workflow:
                        print("debian-packages","Looking at",e)
                        if e not in sample_workflow:
                            sample_workflow.append(e)
                else:
                    sample_workflow=workflow          

            if build_components["oci-images"]["minion"] or \
               build_components["oci-images"]["horizon"] or \
               build_components["oci-images"]["sentinel"]:
                libyaml.clean()
                
                if     build_components["oci-images"]["minion"] or \
                   not build_components["oci-images"]["horizon"] or \
                   not build_components["oci-images"]["sentinel"]:
                   print("oci-images > minion:",libyaml.tell_extended_requirements('minion-image'))
                   workflow=libyaml.generate_workflows(workflow_data,"minion-image",level,sample_workflow,enable_filters=False)
                elif not build_components["oci-images"]["minion"] or \
                         build_components["oci-images"]["horizon"] or \
                     not build_components["oci-images"]["sentinel"]:
                     print("oci-images > horizon:",libyaml.tell_extended_requirements('horizon-image'))
                     workflow=libyaml.generate_workflows(workflow_data,"horizon-image",level,sample_workflow,enable_filters=False)
                elif not build_components["oci-images"]["minion"] or \
                     not build_components["oci-images"]["horizon"] or \
                         build_components["oci-images"]["sentinel"]:
                         print("oci-images > sentinel:",libyaml.tell_extended_requirements('sentinel-image'))
                         workflow=libyaml.generate_workflows(workflow_data,"sentinel-image",level,sample_workflow,enable_filters=False)
                else:
                    print("oci-images > all:",libyaml.tell_extended_requirements('oci'))
                    workflow=libyaml.generate_workflows(workflow_data,"oci",level,sample_workflow,enable_filters=False)

                if len(sample_workflow)>1:
                    for e in workflow:
                        print("oci-images","Looking at",e)
                        if e not in sample_workflow:
                            sample_workflow.append(e)
                else:
                    sample_workflow=workflow   

            if build_components["experimental"]:
                libyaml.clean()
                print("experimental:",libyaml.tell_extended_requirements('experimental'))
                workflow=libyaml.generate_workflows(workflow_data,"experimental",level,sample_workflow,enable_filters=False)
                if len(sample_workflow)>1:
                    for e in workflow:
                        print("doc","Looking at",e)
                        if e not in sample_workflow:
                            sample_workflow.append(e)
                else:
                    sample_workflow=workflow                
                sample_workflow=workflow                
            if build_components["build"]["build"]:
                print("build> build:",libyaml.tell_extended_requirements('build'))
                workflow=libyaml.generate_workflows(workflow_data,"build",level,sample_workflow)
                if len(sample_workflow)>1:
                    for e in workflow:
                        print("build","Looking at",e)
                        if e not in sample_workflow:
                            sample_workflow.append(e)
                else:
                    sample_workflow=workflow
            if build_components["build"]["docs"]:
                libyaml.clean()
                print("build> docs :",libyaml.tell_extended_requirements('docs'))
                workflow=libyaml.generate_workflows(workflow_data,"docs",level,sample_workflow)
                if len(sample_workflow)>1:
                    for e in workflow:
                        print("docs","Looking at",e)
                        if e not in sample_workflow:
                            sample_workflow.append(e)
                else:
                    sample_workflow=workflow
            if build_components["build"]["ui"]:
                libyaml.clean()
                print("build> ui :",libyaml.tell_extended_requirements('ui'))
                workflow=libyaml.generate_workflows(workflow_data,"ui",level,sample_workflow)
                if len(sample_workflow)>1:
                    for e in workflow:
                        print("ui","Looking at",e)
                        if e not in sample_workflow:
                            sample_workflow.append(e)
                else:
                    sample_workflow=workflow
            if build_components["build"]["coverage"]:
                print("build> coverage :",libyaml.tell_extended_requirements('weekly-coverage'))
                workflow=libyaml.generate_workflows(workflow_data,"weekly-coverage",level,sample_workflow)
                if len(sample_workflow)>1:
                    for e in workflow:
                        print("weekly-coverage","Looking at",e)
                        if e not in sample_workflow:
                            sample_workflow.append(e)
                else:
                    sample_workflow=workflow
            if build_components["publish"]["packages"]:
                libyaml.clean()
                print("publish> packages :",libyaml.tell_extended_requirements('build-deploy'))
                workflow=libyaml.generate_workflows(workflow_data,"build-deploy",level,sample_workflow)
                if len(sample_workflow)>1:
                    for e in workflow:
                        print("packages","Looking at",e)
                        if e not in sample_workflow:
                            sample_workflow.append(e)
                else:
                    sample_workflow=workflow

            if not build_components["build"]["build"] and not build_components["build"]["docs"] and not build_components["build"]["ui"] and not build_components["build"]["coverage"]:
                print("empty:",libyaml.tell_extended_requirements('empty'))
                sample_workflow=libyaml.generate_workflows(workflow_data,"empty",level,sample_workflow,enable_filters=True)
        
            if sample_workflow:
                for line in sample_workflow:
                    if type(line)==list:
                        for entry_lvl2 in line:
                            if type(entry_lvl2) == list:
                                for entry_lvl3 in entry_lvl2:
                                    final_output+=entry_lvl3+"\n"
                            else:
                                final_output+=entry_lvl2+"\n"
                    else:
                        final_output+=line+"\n"
            continue

        block_type,step=re_match.group().split(":")
        commands=keywords[block_type.replace("#","").strip()][re_match.group().strip()]["commands"]

        for command in commands:
            if type(command) == list:
                for sub_command in command:
                    final_output+=sub_command
            else:
                final_output+=command
    else:
        final_output+=e

final_output+="\n"
executors_yml_content=common_library.read_file(path_to_executors_yml)
for e in executors_yml_content:
    final_output+=e

final_output+="\n"
parameters_yml_content=common_library.read_file(path_to_parameters_yml)
for e in parameters_yml_content:
    final_output+=e


common_library.write_file(path_to_modified_main,final_output)


os.remove(os.path.join(working_directory.name,".circleci","main","@main.yml"))
os.remove(os.path.join(working_directory.name,".circleci","main","executors.yml"))
os.remove(os.path.join(working_directory.name,".circleci","main","parameters.yml"))

## move the .circleci with updated main.yml file into tmp directory
shutil.move(os.path.join(working_directory.name,".circleci"),"/tmp/")

for folder in component_folders:
    shutil.rmtree(os.path.join("/tmp",".circleci","main",folder))

working_directory.cleanup()


