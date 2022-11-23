<template>
  <div style="margin-top: 10px; margin-bottom: 80px">
    <div style="margin: 10px 0">
      <el-input v-model="search" placeholder="请输入关键字" style="width: 20%" clearable></el-input>

      <el-button type="primary" style="margin-left: 5px" @click="load">查询</el-button>
      <el-button type="primary" @click="handleTest2">模型测试</el-button>
    </div>
    <el-table
        v-loading="loading2"
        :data="tableData2"
        border
        stripe
        size="mini"
        style="width: 100%">
      <el-table-column
          prop="title" min-width="200px"
          label="标题">
      </el-table-column>
      <el-table-column
          prop="newsFrom"
          label="来源">
      </el-table-column>
      <el-table-column
          prop="memberName"
          label="作者">
      </el-table-column>
      <el-table-column
          prop="catName"
          label="类型">
      </el-table-column>
      <el-table-column
          prop="typeName"
          label="类别">
      </el-table-column>
      <el-table-column
          prop="newsStatusStr"
          label="审核状态">
      </el-table-column>
<!--      <el-table-column-->
<!--          prop="time"-->
<!--          label="时间">-->
<!--      </el-table-column>-->
      <el-table-column label="操作" min-width="100px">
        <template #default="scope">
          <el-button size="mini" @click="qgfx(scope.row.id)">查看分析结果</el-button>
<!--          <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>-->
        </template>
      </el-table-column>

    </el-table>

    <div style="margin: 10px 0">
      <el-pagination
          @size-change="handleSizeChange2"
          @current-change="handleCurrentChange2"
          :current-page="currentPage2"
          :page-sizes="[5, 10, 20]"
          :page-size="pageSize2"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total2">
      </el-pagination>
    </div>

    <div v-if="showTask">
    <el-row :gutter="10">
      <el-col :span="6">
        <el-card v-if="zm!=-1">
          任务状态 : <el-tag>已完成</el-tag>
        </el-card>
        <el-card v-if="zm==-1">
          任务状态 : <el-tag>未完成</el-tag>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card v-if="zm == 1">
          是否为负面 : <el-tag type="success">否</el-tag>
        </el-card>
        <el-card v-if="zm == 0">
          是否为负面 : <el-tag type="danger">是</el-tag>
        </el-card>
<!--        <el-input >-->
      </el-col>

      <el-col :span="12" >
        <el-card>
          <div id="myChart" :style="{width: '400px', height: '200px'}"></div>
        </el-card>
      </el-col>
    </el-row>

      <el-table
          v-loading="loading"
          :data="tableData"
          border
          stripe
          style="width: 100%">

        <el-table-column
            prop="title" min-width="200px"
            label="新闻标题">
        </el-table-column>
        <el-table-column
            prop="content" min-width="200px"
            label="评论内容">
        </el-table-column>
        <el-table-column
            prop="username"
            label="评论人">
        </el-table-column>
        <el-table-column
            prop="zmdf"
            label="积极概率">
        </el-table-column>
        <el-table-column
            prop="fmdf"
            label="消极概率">
        </el-table-column>

        <el-table-column label="操作" min-width="100px">
          <template #default="scope">
            <el-button size="mini" @click="handleTest(scope.row.id)">测试</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin: 10px 0">
        <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-sizes="[5, 10, 20]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total">
        </el-pagination>
      </div>
    </div>

    <el-dialog title="提示" v-model="dialogVisible" width="50%" >
      <el-form :model="form" label-width="120px">
        <el-form-item label="内容">
          <el-input v-model="form.title" style="width: 50%"></el-input>
        </el-form-item>
        <el-form-item label="结果" v-if="tzmdf>0">
          积极:{{tzmdf}}-消极:{{tfmdf}}
        </el-form-item>
      </el-form>
      <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">取 消</el-button>
            <el-button type="primary" @click="test2">确 定</el-button>
          </span>
      </template>
    </el-dialog>

  </div>
</template>

<script>
import request from "@/utils/request";

export default {
  name: "Message",
  data() {
    return {
      showTask:false,
      news:[],
      dialogVisible:false,
      loading: true,
      form: {},
      search: '',
      currentPage: 1,
      pageSize: 5,
      total: 0,
      zm : -1,
      tzmdf: 0 ,
      tfmdf: 0,
      zmdf : 0,
      fmdf : 0,
      tableData: [],
      vis: false,
      detail: {},
      user: {},
      messages: [],
      dialogFormVisible: false,
      isCollapse: false,
      entity: {},
      loading2:false,
      tableData2:[],
      total2: 0,
      currentPage2: 1,
      pageSize2: 5,
    }
  },
  created() {
    this.user = sessionStorage.getItem("user") ? JSON.parse(sessionStorage.getItem("user")) : {};
    // this.loadMessage();
    this.load();


  },
  methods: {
    qgfx(id){
      this.showTask = false
      this.loadMessage(id);
    },
    load() {
      this.loading2 = true
      request.get("/news", {
        params: {
          pageNum: this.currentPage2,
          pageSize: this.pageSize2,
          search: this.search
        }
      }).then(res => {
        this.loading2 = false
        this.tableData2 = res.data.records
        this.total2 = res.data.total
      })
    },
    loadMessage(id) {
      // 如果是留言的话，就写死=0
      // 如果是 评论，则需要设置 当前被评论的模块的id作为foreignId
      // let foreignId = 0;
      // request.get("/newsMessage/foreign/" + foreignId).then(res => {
      //   this.messages = res.data;
      // })
      this.loading = true
      request.get("/newsMessage/page", {
        params: {
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          search: this.search,
          newsId:id
        }
      }).then(res => {
        this.loading = false
        if(res.data.page.total>0){
          this.tableData = res.data.page.records
          this.total = res.data.page.total
          this.zm = res.data.zm
          this.zmdf = res.data.zmdf
          this.fmdf = res.data.fmdf
          this.showTask = true;
          setTimeout(()=>{
            this.drawLine()
          },2000)
        }
        else{
          this.$message({
            type: "error",
            message: "没有评论"
          })
        }

      })
    },
    drawLine() {
      // 基于准备好的dom，初始化echarts实例
      let myChart = this.$root.echarts.init(document.getElementById('myChart'))
      let option = {
        title: {
          text: '情感分析',
          subtext: '实时数据',
          left: 'left'
        },
        tooltip: {
          trigger: 'item'
        },
        legend: {
          orient: 'vertical',
          trigger: 'item',
          left: 'center'
        },
        toolbox: {
          show: true,
          feature: {
            mark: {show: true},
            dataView: {show: true, readOnly: false},
            restore: {show: true},
            saveAsImage: {show: true}
          }
        },
        series: [
          {
            name: '得分比例',
            type: 'pie',
            radius: [50, 150],
            center: ['50%', '60%'],
            roseType: 'area',
            itemStyle: {
              borderRadius: 8
            },
            data: [
                {name: "正面", value: this.zmdf},{name: "负面", value: this.fmdf}
            ]
          }
        ]
      }
      // option.series[0].data.push({name: "正面", value: 98.333})
      // option.series[1].data.push({name: "负面", value: 1.667})
      // 绘制图表
      myChart.setOption(option);
    },
    cancel() {
      this.dialogFormVisible = false;
      this.entity = {};
    },
    reReply(id) {
      this.dialogFormVisible = true;
      this.entity.parentId = id;
    },
    reply() {
      this.entity.content = this.entity.reply;
      this.save();
    },

    save() {
      if (!this.user.username) {
        this.$message({
          message: "请登录",
          type: "warning"
        });
        return;
      }
      if (!this.entity.content) {
        this.$message({
          message: "请填写内容",
          type: "warning"
        });
        return;
      }
      // 如果是评论的话，在 save的时候要注意设置 当前模块的id为 foreignId。也就是  entity.foreignId = 模块id
      request.post("/newsMessage", this.entity).then(res => {
        if (res.code === '0') {
          this.$message({
            message: "评论成功",
            type: "success"
          });
        } else {
          this.$message({
            message: res.msg,
            type: "error"
          });
        }
        this.entity = {}
        this.loadMessage();
        this.dialogFormVisible = false;
      })
    },
    handleSizeChange(pageSize) {   // 改变当前每页的个数触发
      this.pageSize = pageSize
      this.loadMessage()
    },
    handleCurrentChange(pageNum) {  // 改变当前页码触发
      this.currentPage = pageNum
      this.loadMessage()
    },

    handleSizeChange2(pageSize) {   // 改变当前每页的个数触发
      this.pageSize2 = pageSize
      this.load()
    },
    handleCurrentChange2(pageNum) {  // 改变当前页码触发
      this.currentPage2 = pageNum
      this.load()
    },
    del(id) {
      request.delete("/message/" + id).then(res => {
        this.$message({
          message: "删除成功",
          type: "success"
        });
        this.loadMessage()
      })
    },
    handleTest(id) {
      request.get("/newsMessage/test/" + id).then(res => {
        this.$message({
          message: "计算成功",
          type: "success"
        });
        this.loadMessage()
      })
    },
    handleTest2() {
      this.dialogVisible = true

    },
    test2(){
      request.get("/newsMessage/test2/"+this.form.title).then(res => {
        this.$message({
          message: "计算成功",
          type: "success"
        });
        this.tzmdf = res.data.zmdf
        this.tfmdf = res.data.fmdf
      })
    }
  }
}
</script>
