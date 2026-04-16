<template>
  <div class="address-edit-container">
    <el-container class="address-edit">
      <el-header class="address-header">
        <div class="header-content">
          <el-button link @click="navigateBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <h2>{{ isEdit ? '编辑地址' : '添加地址' }}</h2>
        </div>
      </el-header>
      <el-main class="address-main">
        <el-card class="address-edit-card">
          <el-form
            ref="addressFormRef"
            :model="addressForm"
            :rules="addressRules"
            label-width="100px"
            class="address-form"
          >
            <!-- 收货人姓名 -->
            <el-form-item prop="name" label="收货人">
              <el-input
                v-model="addressForm.name"
                placeholder="请输入收货人姓名"
                :clearable="true"
              />
            </el-form-item>

            <!-- 手机号码 -->
            <el-form-item prop="phone" label="手机号">
              <el-input
                v-model="addressForm.phone"
                placeholder="请输入手机号码"
                :clearable="true"
                maxlength="11"
              />
            </el-form-item>

            <!-- 省市区 -->
            <el-form-item prop="region" label="所在地区">
              <el-cascader
                v-model="regionData"
                :options="regionOptions"
                placeholder="请选择省市区"
                :clearable="true"
                @change="handleRegionChange"
              />
            </el-form-item>

            <!-- 详细地址 -->
            <el-form-item prop="detail" label="详细地址">
              <el-input
                v-model="addressForm.detail"
                type="textarea"
                :rows="3"
                placeholder="请输入详细地址"
                :clearable="true"
              />
            </el-form-item>

            <!-- 是否默认地址 -->
            <el-form-item prop="isDefault" label="是否默认">
              <el-switch v-model="addressForm.isDefault" />
            </el-form-item>

            <!-- 操作按钮 -->
            <el-form-item>
              <el-button type="primary" @click="handleSave">
                <el-icon><DocumentAdd /></el-icon>
                {{ isEdit ? '保存修改' : '保存地址' }}
              </el-button>
              <el-button @click="handleReset">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
              <el-button v-if="isEdit" type="danger" @click="handleDelete">
                <el-icon><Delete /></el-icon>
                删除地址
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAppNavigation } from '@/composables/useAppNavigation'
import { useUserStore } from '@/stores/user'
import { ArrowLeft, DocumentAdd, Refresh, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const { navigate } = useAppNavigation()
const route = useRoute()
const userStore = useUserStore()

// 响应式数据
const addressFormRef = ref()
const addressForm = reactive({
  id: null,
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false
})
const regionData = ref([])

// 表单验证规则
const addressRules = reactive({
  name: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '收货人姓名长度在 2 到 20 个字符之间', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入有效的手机号码',
      trigger: 'blur'
    }
  ],
  province: [
    { required: true, message: '请选择省份', trigger: 'change' }
  ],
  city: [
    { required: true, message: '请选择城市', trigger: 'change' }
  ],
  district: [
    { required: true, message: '请选择区县', trigger: 'change' }
  ],
  detail: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { min: 5, max: 200, message: '详细地址长度在 5 到 200 个字符之间', trigger: 'blur' }
  ]
})

// 省市区三级联动数据
const regionOptions = ref([
  {
    value: '110000',
    label: '北京市',
    children: [
      {
        value: '110100',
        label: '北京市',
        children: [
          { value: '110101', label: '东城区' },
          { value: '110105', label: '朝阳区' },
          { value: '110106', label: '丰台区' },
          { value: '110108', label: '海淀区' },
          { value: '110109', label: '石景山区' },
          { value: '110111', label: '房山区' },
          { value: '110112', label: '通州区' },
          { value: '110113', label: '顺义区' },
          { value: '110114', label: '昌平区' }
        ]
      }
    ]
  },
  {
    value: '330000',
    label: '浙江省',
    children: [
      {
        value: '330100',
        label: '杭州市',
        children: [
          { value: '330102', label: '上城区' },
          { value: '330106', label: '西湖区' },
          { value: '330108', label: '滨江区' },
          { value: '330110', label: '余杭区' },
          { value: '330111', label: '富阳区' },
          { value: '330112', label: '临平区' }
        ]
      },
      {
        value: '330200',
        label: '宁波市',
        children: [
          { value: '330203', label: '海曙区' },
          { value: '330205', label: '江北区' },
          { value: '330212', label: '鄞州区' },
          { value: '330213', label: '奉化区' },
          { value: '330225', label: '象山县' },
          { value: '330226', label: '宁海县' }
        ]
      }
    ]
  }
])

// 计算属性
const isEdit = computed(() => !!route.params.id)

// 方法
const navigateBack = () => {
  navigate('/user/address')
}

const handleRegionChange = (value) => {
  if (value && value.length === 3) {
    // 这里简化处理，实际应该根据地区编码获取对应的省市区名称
    const province = regionOptions.value.find(item => item.value === value[0])
    const city = province?.children.find(item => item.value === value[1])
    const district = city?.children.find(item => item.value === value[2])

    addressForm.province = province?.label || ''
    addressForm.city = city?.label || ''
    addressForm.district = district?.label || ''
  }
}

const handleSave = async () => {
  try {
    const valid = await addressFormRef.value.validate()
    if (!valid) {
      return
    }

    // 调用 API 保存地址
    if (isEdit.value) {
      await userStore.updateAddress(route.params.id, addressForm)
    } else {
      await userStore.addAddress(addressForm)
    }

    ElMessage.success(isEdit.value ? '地址更新成功' : '地址添加成功')
    navigate('/user/address')
  } catch (error) {
    console.error('保存地址失败:', error)
  }
}

const handleReset = async () => {
  try {
    if (isEdit.value) {
      await ElMessageBox.confirm('将重置为原始信息，请确认', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      fetchAddressData()
    } else {
      // 重置表单
      Object.assign(addressForm, {
        name: '',
        phone: '',
        province: '',
        city: '',
        district: '',
        detail: '',
        isDefault: false
      })
      regionData.value = []
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重置失败')
    }
  }
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('将删除该地址，请确认', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await userStore.deleteAddress(route.params.id)
    ElMessage.success('地址删除成功')
    navigate('/user/address')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 从路由参数中获取地址数据
const fetchAddressData = async () => {
  if (isEdit.value) {
    const addressList = await userStore.fetchAddressList()
    const address = addressList.find(item => item.id === parseInt(route.params.id))
    if (address) {
      Object.assign(addressForm, {
        id: address.id,
        name: address.name,
        phone: address.phone,
        province: address.province,
        city: address.city,
        district: address.district,
        detail: address.detail,
        isDefault: address.isDefault
      })

      // 设置省市区级联数据
      const province = regionOptions.value.find(item => item.label === address.province)
      if (province) {
        const city = province.children.find(item => item.label === address.city)
        if (city) {
          const district = city.children.find(item => item.label === address.district)
          if (district) {
            regionData.value = [province.value, city.value, district.value]
          }
        }
      }
    }
  }
}

// 钩子函数
onMounted(() => {
  if (isEdit.value) {
    fetchAddressData()
  }
})
</script>

<style scoped>
.address-edit-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.address-edit {
  min-height: 100vh;
}

.address-header {
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 0;
}

.header-content {
  display: flex;
  align-items: center;
  height: 60px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.header-content h2 {
  margin: 0 0 0 20px;
}

.address-main {
  padding: 20px;
}

.address-edit-card {
  max-width: 800px;
  margin: 0 auto;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.address-form {
  margin-top: 20px;
}
</style>
