<template>
    <div>
        <h1 style = "margin-left:4.5%; margin-top:-10px;">Order</h1>
        <v-col style="margin-bottom:40px;">
            <div class="text-center">
                <v-dialog
                        v-model="openDialog"
                        width="332.5"
                        fullscreen
                        hide-overlay
                        transition="dialog-bottom-transition"
                >
                    <Order :offline="offline" class="video-card" :isNew="true" :editMode="true" v-model="newValue" 
                            @add="append" v-if="tick"/>

                    <v-btn
                            style="postition:absolute; top:2%; right:2%"
                            @click="closeDialog()"
                            depressed
                            icon 
                            absolute
                    >
                        <v-icon>mdi-close</v-icon>
                    </v-btn>
                </v-dialog>

                <v-btn  color="blue" fab dark large
                        style="position:fixed; bottom: 5%; right: 2%; z-index:99"
                        @click="openDialog=true;"
                >
                    <v-icon>mdi-plus</v-icon>
                </v-btn>
            </div>
        </v-col>
        <v-row>
            <Order :offline="offline" class="video-card" v-for="(value, index) in values" v-model="values[index]" v-bind:key="index" @delete="remove"/>
        </v-row>
    </div>
</template>

<script>

    const axios = require('axios').default;
    import Order from './../Order.vue';

    export default {
        name: 'OrderManager',
        components: {
            Order,
        },
        props: {
            offline: Boolean
        },
        data: () => ({
            values: [],
            newValue: {},
            tick : true,
            openDialog : false,
        }),
        async created() {
            var me = this;
            if(me.offline){
                if(!me.values) me.values = [];
                return;
            } 

            var temp = await axios.get(axios.fixUrl('/orders'))
            me.values = temp.data._embedded.orders;
            
            me.newValue = {
                'currencyId': '',
                'amount': 0,
                'status': '',
            }
        },
        methods:{
            closeDialog(){
                this.openDialog = false
            },
            append(value){
                this.tick = false
                this.newValue = {}
                this.values.push(value)
                
                this.$emit('input', this.values);

                this.$nextTick(function(){
                    this.tick=true
                })
            },
            remove(value){
                var where = -1;
                for(var i=0; i<this.values.length; i++){
                    if(this.values[i]._links.self.href == value._links.self.href){
                        where = i;
                        break;
                    }
                }

                if(where > -1){
                    this.values.splice(i, 1);
                    this.$emit('input', this.values);
                }
            },
        }
    };
</script>


<style>
    .video-card {
        width:300px; 
        margin-left:4.5%; 
        margin-top:50px; 
        margin-bottom:50px;
    }
</style>

