/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.wayang.agoraeo.operators.java;

import org.apache.wayang.agoraeo.operators.basic.SentinelSource;
import org.apache.wayang.core.optimizer.OptimizationContext.OperatorContext;
import org.apache.wayang.core.plan.wayangplan.ExecutionOperator;
import org.apache.wayang.core.platform.ChannelDescriptor;
import org.apache.wayang.core.platform.ChannelInstance;
import org.apache.wayang.core.platform.lineage.ExecutionLineageNode;
import org.apache.wayang.core.util.Tuple;
import org.apache.wayang.java.channels.StreamChannel;
import org.apache.wayang.java.execution.JavaExecutor;
import org.apache.wayang.java.operators.JavaExecutionOperator;

import java.util.*;
import java.util.stream.StreamSupport;

//TODO add the documentation and add the Profile Estimator
public class JavaSentinelSource
    extends SentinelSource
    implements JavaExecutionOperator {

  public JavaSentinelSource(String iterator) {
    super(iterator);
  }

  public JavaSentinelSource(SentinelSource that) {
    super(that);
  }

  @Override
  public List<ChannelDescriptor> getSupportedInputChannels(int index) {
    throw new UnsupportedOperationException(
        String.format(
            "%s does not have input channels.",
            this
        )
    );
  }

  @Override
  public List<ChannelDescriptor> getSupportedOutputChannels(int index) {
    assert index <= this.getNumOutputs() || (index == 0 && this.getNumOutputs() == 0);
    return Collections.singletonList(StreamChannel.DESCRIPTOR);
  }

  @Override
  public Tuple<Collection<ExecutionLineageNode>, Collection<ChannelInstance>> evaluate(
      ChannelInstance[] inputs, ChannelInstance[] outputs,
      JavaExecutor javaExecutor, OperatorContext operatorContext) {
    assert inputs.length == this.getNumInputs();
    assert outputs.length == this.getNumOutputs();

    ((StreamChannel.Instance) outputs[0]).accept(
      StreamSupport.stream(
          Spliterators.spliteratorUnknownSize(
              this.getIterator(),
              Spliterator.ORDERED
          ),
          false
      )
    );

    return ExecutionOperator.modelLazyExecution(
        inputs,
        outputs,
        operatorContext
    );

  }
}
