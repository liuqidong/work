package com.liuqidong.activiti.demo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.activiti.engine.FormService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.form.DateFormType;
import org.activiti.engine.impl.form.StringFormType;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class DemoMain {
	private static final Logger LOGGER = LoggerFactory.getLogger(DemoMain.class);
	
	public static void main(String[] args) throws ParseException {
		LOGGER.info("启动程序");
		//1.创建流程引擎
		ProcessEngine processEngine = getProcessEngine();
		
		//2.部署流程定义文件
		ProcessDefinition processDefinition = getProcessDefinition(processEngine);
		
		//3.启动运行流程
		ProcessInstance processInstance = getProcessInstance(processEngine, processDefinition);
		
		//4.处理流程任务
		processTask(processEngine, processInstance);
		LOGGER.info("结束程序");
	}

	private static void processTask(ProcessEngine processEngine, ProcessInstance processInstance)
			throws ParseException {
		Scanner scanner = new Scanner(System.in);
		while(processInstance != null && !processInstance.isEnded()) {
			TaskService taskService = processEngine.getTaskService();
			List<Task> tasks = taskService.createTaskQuery().list();
			LOGGER.info("待处理任务数量[{}]",tasks.size());
			for (Task task : tasks) {
				LOGGER.info("待处理任务 [{}]",task.getName());
				Map<String, Object> variables = getMap(processEngine, scanner, task);
				taskService.complete(task.getId(), variables);
				
				processInstance = processEngine.getRuntimeService().createProcessInstanceQuery().singleResult();
			}
		}
		scanner.close();
	}

	private static Map<String, Object> getMap(ProcessEngine processEngine, Scanner scanner, Task task)
			throws ParseException {
		FormService formService = processEngine.getFormService();
		TaskFormData taskFormData = formService.getTaskFormData(task.getId());
		List<FormProperty> formProperties = taskFormData.getFormProperties();
		Map<String, Object> variables = Maps.newHashMap();
		for (FormProperty formProperty : formProperties) {
			String line = null;
			if(StringFormType.class.isInstance(formProperty.getType())) {
				LOGGER.info("请输入 {} ?", formProperty.getName());
				line = scanner.nextLine();
				variables.put(formProperty.getId(), line);
			}else if(DateFormType.class.isInstance(formProperty.getType())) {
				LOGGER.info("请输入 {} ? (yyyy-MM-dd)", formProperty.getName());
				line = scanner.nextLine();
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
				Date date = simpleDateFormat.parse(line);
				variables.put(formProperty.getId(), date);
			}else {
				LOGGER.info("类型暂不支持 {}", formProperty.getType());
			}
			LOGGER.info("您输入的内容是 [{}]",line);
		}
		return variables;
	}

	private static ProcessInstance getProcessInstance(ProcessEngine processEngine, ProcessDefinition processDefinition) {
		RuntimeService runtimeService = processEngine.getRuntimeService();
		ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
		LOGGER.info("启动流程[{}]",processInstance.getProcessDefinitionKey());
		return processInstance;
	}

	private static ProcessDefinition getProcessDefinition(ProcessEngine processEngine) {
		RepositoryService repositoryService = processEngine.getRepositoryService();
		DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
		deploymentBuilder.addClasspathResource("second_apprvoe.bpmn");
		Deployment deployment = deploymentBuilder.deploy();
		String deploymentId = deployment.getId();
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
			.deploymentId(deploymentId).singleResult();
		LOGGER.info("流程定义文件 [{}],流程Id [{}]",processDefinition.getName(),processDefinition.getId());
		return processDefinition;
	}

	private static ProcessEngine getProcessEngine() {
		ProcessEngineConfiguration cfg = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
		ProcessEngine processEngine = cfg.buildProcessEngine();
		String name = processEngine.getName();
		String version = processEngine.VERSION;
		LOGGER.info("流程引擎名称{},版本{}",name,version);
		return processEngine;
	}
}
