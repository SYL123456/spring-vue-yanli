<template>
  <div style="margin-top: 10px; margin-bottom: 80px">
      <div style="padding: 20px; color: #888">
        <div>
          <el-radio-group @change="loadMessage" v-model="queryMstatus">
            <el-radio-button  label="-1">全部</el-radio-button>
            <el-radio-button  label="0">待审核</el-radio-button>
            <el-radio-button  label="1">审核通过</el-radio-button>
          </el-radio-group>
        </div>
      </div>
      <el-table
          v-loading="loading"
          :data="tableData"
          border
          stripe
          style="width: 100%">
        <el-table-column
            prop="title" min-width="100px"
            label="新闻标题">
        </el-table-column>
        <el-table-column
            prop="content" min-width="100px"
            label="内容">
        </el-table-column>
        <el-table-column
            prop="username"
            label="评论人">
        </el-table-column>
        <el-table-column
            label="审核状态">
          <template #default="scope">
            <span v-if="scope.row.messageStatus == 0"><el-tag type="danger">待审核</el-tag></span>
            <span v-if="scope.row.messageStatus == 1"><el-tag type="success">审核通过</el-tag></span>
          </template>
        </el-table-column>
        <el-table-column
            prop="time"  min-width="100px"
            label="时间">
        </el-table-column>
        <el-table-column label="审核" min-width="100px">
          <template #default="scope">
            <el-popconfirm title="确定上线吗？" v-if="scope.row.messageStatus != 1" @confirm="updateStatusUp(scope.row)">
              <template #reference>
                <el-button size="mini" >通过</el-button>
              </template>
            </el-popconfirm>
            <el-popconfirm title="确定下线吗？" v-if="scope.row.messageStatus != 0" @confirm="updateStatusDown(scope.row)">
              <template #reference>
                <el-button size="mini" type="danger">下线</el-button>
              </template>
            </el-popconfirm>
            <el-popconfirm title="确定删除吗？" @confirm="del(scope.row.id)">
              <template #reference>
                <el-button size="mini" >删除</el-button>
              </template>
            </el-popconfirm>
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
</template>

<script>
import request from "@/utils/request";

export default {
  name: "Message",
  data() {
    return {
      queryMstatus:0,
      loading: true,
      form: {},
      search: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      zm: 0,
      tableData: [],
      vis: false,
      detail: {},
      user: {},
      messages: [],
      dialogFormVisible: false,
      isCollapse: false,
      entity: {}
    }
  },
  created() {
    this.user = sessionStorage.getItem("user") ? JSON.parse(sessionStorage.getItem("user")) : {};
    this.loadMessage();


  },
  methods: {
    loadMessage() {
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
          queryMstatus: this.queryMstatus,
          search: this.search
        }
      }).then(res => {
        this.loading = false
        this.tableData = res.data.page.records
        this.total = res.data.page.total
        this.zm = res.data.zm
      })
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
    updateStatusUp(row){
      let n = row
      // this.$set(n, "newsStatus", 1);
      request.post("/newsMessage/updateStatusUp", n).then(res => {
        console.log(res)
        if (res.code === '0') {
          this.$message({
            type: "success",
            message: "更新成功"
          })
        } else {
          this.$message({
            type: "error",
            message: res.msg
          })
        }

        this.loadMessage()
      })

    },
    //下线
    updateStatusDown(row){
      let n = row
      request.post("/newsMessage/updateStatusDown", n).then(res => {
        console.log(res)
        if (res.code === '0') {
          this.$message({
            type: "success",
            message: "更新成功"
          })
        } else {
          this.$message({
            type: "error",
            message: res.msg
          })
        }

        this.loadMessage()
      })
    },
    del(id) {
      request.delete("/newsMessage/" + id).then(res => {
        this.$message({
          message: "删除成功",
          type: "success"
        });
        this.loadMessage()
      })
    }
  }
}
</script>
