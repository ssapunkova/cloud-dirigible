/*******************************************************************************
 * Copyright (c) 2014 SAP AG or an SAP affiliate company. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *******************************************************************************/

package com.sap.dirigible.runtime.groovy;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.dirigible.repository.api.IRepository;
import com.sap.dirigible.repository.api.IResource;

public class RepositoryURLConnection extends URLConnection {
	
	private static final String GROOVY_EXTENSION = ".groovy";
	private IRepository repository;
	private String rootPath;
	private String secondaryRootPath;
	private byte[] content;
	private long  dateInMilliseconds;
	private String name;
	
	private static final Logger logger = LoggerFactory.getLogger(RepositoryURLConnection.class);
	
	protected RepositoryURLConnection(IRepository repository, String rootPath, String secondaryRootPath, String name) {
		super(null);
		this.repository = repository;
		this.rootPath = rootPath;
		this.secondaryRootPath = secondaryRootPath;
		this.name = name;
	}
	
	/**
	 * The method makes connection with the repository in order to retrieve the requested resource.
	 */
	@Override
	public void connect() throws IOException {
			//project1.module1 should be project1/module1 in order to retrieve it from the repository
		
			if (name.endsWith(".groovy")){
				int idx = name.lastIndexOf(".");
				name = name.substring(0,idx).replace(".", IRepository.SEPARATOR)+".groovy";
				name = name.replace("$",IRepository.SEPARATOR);
				
			}
			else{
				name = name.replace(".", IRepository.SEPARATOR);
				name = name.replace("$",IRepository.SEPARATOR);
			}	
			
			IResource resource = retrieveResource(name);
			if (resource.exists()) {
				content = resource.getContent();
				dateInMilliseconds = resource.getInformation().getModifiedAt().getTime();
			} else {
				throw new FileNotFoundException("Resource: "+name+" does not exist");
			}
	}
	
	/**
	 * The method returns the time of change of the last-modified header field. The result is the number of milliseconds since January 1, 1970 GMT.
	 */
	public long getLastModifed() throws IOException {
		return dateInMilliseconds;
	}
	
	/**
	 * The method returns the resource content as byte array. 
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		if (!connected)
			connect();
		return new ByteArrayInputStream(content);
	}
	
	/**
	 * The method retrieves a resource by name form the repository.. 
	 * @throws IOException 
	 */
	private IResource retrieveResource(String name) throws IOException {
		String repositoryPath = this.rootPath + IRepository.SEPARATOR + name;
		if (!repositoryPath.endsWith(GROOVY_EXTENSION)) {
			repositoryPath += GROOVY_EXTENSION;
		}
		IResource resource = this.repository.getResource(repositoryPath);
		if (!resource.exists()) {
			repositoryPath = this.secondaryRootPath + IRepository.SEPARATOR + name;
			if (!repositoryPath.endsWith(GROOVY_EXTENSION)) {
				repositoryPath += GROOVY_EXTENSION;
			}
			resource = this.repository.getResource(repositoryPath);
			if (!resource.exists()) {
				logger.error("There is no resource at the specified Service path: " + repositoryPath);
			}
		}
		return resource;
	}
}  
