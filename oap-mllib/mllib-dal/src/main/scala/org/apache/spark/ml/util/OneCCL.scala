/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.ml.util

import org.apache.spark.SparkConf

object OneCCL {

  var cclParam = new CCLParam()

  var kvsIPPort = sys.env.getOrElse("CCL_KVS_IP_PORT", "")
  var worldSize = sys.env.getOrElse("CCL_WORLD_SIZE", "1").toInt

  val KVS_PORT = 51234

  private def checkEnv() {
    val altTransport = sys.env.getOrElse("CCL_ATL_TRANSPORT", "")
    val pmType = sys.env.getOrElse("CCL_PM_TYPE", "")
    val ipExchange = sys.env.getOrElse("CCL_KVS_IP_EXCHANGE", "")

    assert(altTransport == "ofi")
    assert(pmType == "resizable")
    assert(ipExchange == "env")
    assert(kvsIPPort != "")

  }

  // Run on Driver
  def setDefaultExecutorEnv(): Unit = {

    val conf = new SparkConf(true)

    val ccl_root_path = "/home/xiaochang/Works/mlsl2/build/_install"
    val ccl_root_ipport = "10.239.44.22_3000"

    conf.setExecutorEnv("CCL_PM_TYPE","resizable")
      .setExecutorEnv("CCL_ATL_TRANSPORT","ofi")
      .setExecutorEnv("CCL_KVS_IP_EXCHANGE","env")
      .setExecutorEnv("CCL_KVS_IP_PORT", ccl_root_ipport)
      .setExecutorEnv("CCL_ROOT", ccl_root_path)
      .setExecutorEnv("CCL_WORLD_SIZE", Utils.sparkExecutorNum().toString)
      .setExecutorEnv("I_MPI_ROOT", ccl_root_path)
      .setExecutorEnv("CCL_ATL_TRANSPORT_PATH", s"$ccl_root_path/lib")
      .setExecutorEnv("FI_PROVIDER_PATH",s"$ccl_root_path/lib/prov")
  }

  // Run on Executor
  def setExecutorEnv(executor_num: Int, ip: String, port: Int): Unit = {
    // TODO: pack *.so inside jar
    val ccl_root_path = "/home/xiaochang/Works/mlsl2/build/_install"
//    val ccl_root_path = "./lib/oneccl"

    // Work around ccl by passings in a spark.executorEnv.CCL_KVS_IP_PORT. eg: 10.0.0.138_3000
    val ccl_kvs_ip_port = sys.env.getOrElse("CCL_KVS_IP_PORT", s"${ip}_${port}")

    println(s"oneCCL: Initializing with CCL_KVS_IP_PORT: $ccl_kvs_ip_port")

    setEnv("CCL_PM_TYPE", "resizable")
    setEnv("CCL_ATL_TRANSPORT","ofi")
    setEnv("CCL_KVS_IP_EXCHANGE","env")
    setEnv("CCL_KVS_IP_PORT", ccl_kvs_ip_port)
    setEnv("CCL_WORLD_SIZE", s"${executor_num}")
//    setEnv("I_MPI_ROOT", ccl_root_path)
//    setEnv("CCL_ATL_TRANSPORT_PATH", s"$ccl_root_path/lib")
//    setEnv("FI_PROVIDER_PATH",s"$ccl_root_path/lib/prov")
  }

  def init(executor_num: Int, ip: String, port: Int) = {

    setExecutorEnv(executor_num, ip, port)

    // cclParam is output from native code
    c_init(cclParam)

    // executor number should equal to oneCCL world size
    assert(executor_num == cclParam.commSize, "executor number should equal to oneCCL world size")

    println(s"oneCCL: Initialized with executorNum: $executor_num, commSize, ${cclParam.commSize}, rankId: ${cclParam.rankId}")

  }

  // Run on Executor
  def cleanup(): Unit = {
    c_cleanup()
  }

  @native private def c_init(param: CCLParam) : Int
  @native private def c_cleanup() : Unit

  @native def isRoot() : Boolean
  @native def rankID() : Int

  @native def setEnv(key: String, value: String, overwrite: Boolean = true): Int
}